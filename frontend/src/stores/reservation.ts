import {defineStore} from 'pinia'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import {bookSeat, reserveSeat} from '@/api/kinoApi'
import type {Booking, BookSeatRequest, Reservation, Screening, SeatMap} from '@/api/types'
import {useScreeningStore} from "@/stores/screening.ts";

const RESERVATION_KEY = 'holi-kino.reservation'
const BOOKING_KEY = 'holi-kino.booking'
const BOOKED_SCREENING_KEY = 'holi-kino.booked-screening'

// The form data of a booking; the token comes from the current reservation.
export type BookingFormValues = Omit<BookSeatRequest, 'reservationToken'>

function readStored<T>(key: string): T | null {
  try {
    const raw = sessionStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

function writeStored(key: string, value: unknown): void {
  if (value) {
    sessionStorage.setItem(key, JSON.stringify(value))
  } else {
    sessionStorage.removeItem(key)
  }
}

function isExpired(reservation: Reservation): boolean {
  return new Date(reservation.reservedUntil).getTime() <= Date.now()
}

// Restore a reservation after a page reload, as long as its deadline has not passed yet.
function loadStoredReservation(): Reservation | null {
  const stored = readStored<Reservation>(RESERVATION_KEY)
  return stored && !isExpired(stored) ? stored : null
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
      // The last completed booking. Kept in sessionStorage: without e-mail it is the guest's only confirmation.
      booking: readStored<Booking>(BOOKING_KEY),
      // A copy of the screening at the time of booking, like a receipt: independent of the server.
      bookedScreening: readStored<Screening>(BOOKED_SCREENING_KEY),
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

    // Turns the current reservation into a booking. Errors are passed on, so the form can show them.
    async book(form: BookingFormValues): Promise<void> {
      const reservation = this.reservation
      if (!reservation) {
        throw new Error('There is no reservation to book.')
      }
      try {
        const booking = await bookSeat({...form, reservationToken: reservation.reservationToken})
        const selected = useScreeningStore().selected
        this.setBooking(booking, selected?.id === booking.screeningId ? selected : null)
        this.clear()
      } catch (error) {
        if (error instanceof ApiError && error.problem.title === PROBLEM_TITLES.reservationNotActive) {
          this.expire()
        }
        throw error
      }
    },

    // Compare with what the server reports. The server is the source of truth.
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
      writeStored(RESERVATION_KEY, reservation)
      if (reservation) {
        this.expired = false
      }
    },

    setBooking(booking: Booking | null, screening: Screening | null = null): void {
      this.booking = booking
      this.bookedScreening = screening
      writeStored(BOOKING_KEY, booking)
      writeStored(BOOKED_SCREENING_KEY, screening)
    },
  },
})
