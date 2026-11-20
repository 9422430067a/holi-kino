// These types mirror the Java records of the backend (the JSON field names must match exactly).
// Points in time (Java Instant) arrive as ISO strings in UTC, for example "2036-03-02T13:00:00Z".

export interface Screening {
  id: number
  movieTitle: string
  durationMinutes: number
  startsAt: string
}

export type SeatAvailability = 'AVAILABLE' | 'RESERVED' | 'BOOKED'

export interface Seat {
  seatNumber: number
  status: SeatAvailability
}

export interface SeatRow {
  seatRow: number
  seats: Seat[]
}

export interface SeatMap {
  screeningId: number
  rows: SeatRow[]
}

export interface ReserveSeatRequest {
  seatRow: number
  seatNumber: number
  reservationToken?: string // only for a seat change
}

export interface Reservation {
  reservationToken: string
  screeningId: number
  seatRow: number
  seatNumber: number
  reservedUntil: string
}

export interface BookSeatRequest {
  reservationToken: string
  fullName: string
  birthDate: string // "YYYY-MM-DD"
  email: string
  phone?: string
  termsAccepted: boolean
  privacyAccepted: boolean
  newsletterAccepted?: boolean
}

export interface Booking {
  bookingId: string
  screeningId: number
  seatRow: number
  seatNumber: number
}

// Error responses of the backend (RFC 9457). "errors" only exists for invalid form fields.
export interface ProblemDetail {
  title: string
  status: number
  detail?: string
  instance?: string
  errors?: Record<string, string>
}
