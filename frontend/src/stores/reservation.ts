import {defineStore} from 'pinia'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import {reserveSeat} from '@/api/kinoApi'
import type {Reservation, SeatMap} from '@/api/types'

const STORAGE_KEY = 'holi-kino.reservation'

// Restore a reservation after a page reload, as long as its deadline has not passed yet.
function loadStoredReservation(): Reservation | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const stored = JSON.parse(raw) as Reservation
    return isExpired(stored) ? null : stored
  } catch {
    return null
  }
}

function isExpired(reservation: Reservation): boolean {
  return new Date(reservation.reservedUntil).getTime() <= Date.now()
}

// A timer is not part of the state (it is not data), so it lives outside the store.
let expiryTimer: ReturnType<typeof setTimeout> | undefined

// End the reservation exactly at its deadline, no matter which page is open.
function scheduleExpiry(reservation: Reservation | null): void {
  clearTimeout(expiryTimer)
  if (!reservation) return
  const msLeft = new Date(reservation.reservedUntil).getTime() - Date.now()
  expiryTimer = setTimeout(() => useReservationStore().expire(), Math.max(msLeft, 0))
}

export const useReservationStore = defineStore('reservation', {
  state: () => {
    const reservation = loadStoredReservation()
    scheduleExpiry(reservation)
    return {
      reservation,
      // True after a reservation ran out, until the guest reserves again. Lets pages explain what happened.
      expired: false,
    }
  },

  getters: {
    hasReservation: (state) => state.reservation !== null,
  },

  actions: {
    // First reservation without a token, seat change with the token of the current reservation.
    async reserve(screeningId: number, seatRow: number, seatNumber: number): Promise<void> {
      try {
        const reservation = await reserveSeat(screeningId, {
          seatRow,
          seatNumber,
          reservationToken: this.reservation?.reservationToken,
        })
        this.setReservation(reservation)
      } catch (error) {
        // Our token is useless now. "Seat already taken" keeps the current reservation.
        if (error instanceof ApiError && error.problem.title === PROBLEM_TITLES.reservationNotActive) {
          this.expire()
        }
        throw error
      }
    },

    // Compare with what the server reports
    syncWithSeatMap(seatMap: SeatMap): void {
      const reservation = this.reservation
      if (!reservation) return
      // Covers a timer that fired late, for example in a background tab.
      if (isExpired(reservation)) {
        this.expire()
        return
      }
      if (reservation.screeningId !== seatMap.screeningId) return
      const mySeat = seatMap.rows
        .find((row) => row.seatRow === reservation.seatRow)
        ?.seats.find((seat) => seat.seatNumber === reservation.seatNumber)
      if (mySeat?.status === 'AVAILABLE') {
        this.expire()
      }
    },

    expire(): void {
      if (!this.reservation) return
      this.setReservation(null)
      this.expired = true
    },

    clear(): void {
      this.setReservation(null)
    },

    // The only place that changes the reservation, so sessionStorage and the timer always match the state.
    setReservation(reservation: Reservation | null): void {
      this.reservation = reservation
      scheduleExpiry(reservation)
      if (reservation) {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(reservation))
        this.expired = false
      } else {
        sessionStorage.removeItem(STORAGE_KEY)
      }
    },
  },
})
