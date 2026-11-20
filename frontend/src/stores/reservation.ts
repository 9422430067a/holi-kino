import { defineStore } from 'pinia'
import { ApiError, PROBLEM_TITLES } from '@/api/client'
import { reserveSeat } from '@/api/kinoApi'
import type { Reservation } from '@/api/types'

const STORAGE_KEY = 'holi-kino.reservation'

// Restore a reservation after a page reload, as long as its deadline has not passed yet.
function loadStoredReservation(): Reservation | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const stored = JSON.parse(raw) as Reservation
    return new Date(stored.reservedUntil).getTime() > Date.now() ? stored : null
  } catch {
    return null
  }
}

export const useReservationStore = defineStore('reservation', {
  state: () => ({
    reservation: loadStoredReservation(),
  }),

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
        // Our token is useless now, so forget it. "Seat already taken" keeps the current reservation.
        if (error instanceof ApiError && error.problem.title === PROBLEM_TITLES.reservationNotActive) {
          this.clear()
        }
        throw error
      }
    },

    clear(): void {
      this.setReservation(null)
    },

    // The only place that changes the reservation, so sessionStorage always matches the state.
    setReservation(reservation: Reservation | null): void {
      this.reservation = reservation
      if (reservation) {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(reservation))
      } else {
        sessionStorage.removeItem(STORAGE_KEY)
      }
    },
  },
})
