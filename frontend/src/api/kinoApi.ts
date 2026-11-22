import {http} from './client'
import type {
  Booking,
  BookSeatRequest,
  Reservation,
  ReserveSeatRequest,
  Screening,
  SeatMap,
} from './types'

export async function fetchScreenings(): Promise<Screening[]> {
  const {data} = await http.get<Screening[]>('/screenings')
  return data
}

export async function fetchSeatMap(screeningId: number): Promise<SeatMap> {
  const {data} = await http.get<SeatMap>(`/screenings/${screeningId}/seats`)
  return data
}

export async function reserveSeat(screeningId: number, request: ReserveSeatRequest): Promise<Reservation> {
  const {data} = await http.post<Reservation>(`/screenings/${screeningId}/reservations`, request)
  return data
}

export async function bookSeat(request: BookSeatRequest): Promise<Booking> {
  const {data} = await http.post<Booking>('/bookings', request)
  return data
}

export async function fetchScreening(screeningId: number): Promise<Screening> {
  const {data} = await http.get<Screening>(`/screenings/${screeningId}`)
  return data
}
