import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import {reserveSeat} from '@/api/kinoApi'
import type {Reservation, SeatAvailability, SeatMap} from '@/api/types'
import {useReservationStore} from '../reservation'

// Replace the real API calls: these tests are about the store, not the backend.
vi.mock('@/api/kinoApi', () => ({
  reserveSeat: vi.fn(),
  bookSeat: vi.fn(),
}))

const NOW = new Date('2036-03-02T12:00:00Z')
const DEADLINE = '2036-03-02T12:05:00Z'
const FIVE_MINUTES = 5 * 60 * 1000

function reservation(): Reservation {
  return {
    reservationToken: 'token-1',
    screeningId: 7,
    seatRow: 3,
    seatNumber: 4,
    reservedUntil: DEADLINE
  }
}

// A seat map that only contains the guest's own seat with the given status.
function seatMapWithMySeat(status: SeatAvailability): SeatMap {
  return {screeningId: 7, rows: [{seatRow: 3, seats: [{seatNumber: 4, status}]}]}
}

describe('reservation store', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
    sessionStorage.clear()
    setActivePinia(createPinia())
    vi.mocked(reserveSeat).mockResolvedValue(reservation())
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
  })

  it('ends the reservation exactly at its deadline', async () => {
    const store = useReservationStore()
    await store.reserve(7, 3, 4)

    vi.advanceTimersByTime(FIVE_MINUTES - 1)
    expect(store.reservation).not.toBeNull()

    vi.advanceTimersByTime(1)
    expect(store.reservation).toBeNull()
    expect(store.expired).toBe(true)
    expect(sessionStorage.getItem('holi-kino.reservation')).toBeNull()
  })

  it('ends the reservation when the server reports the own seat as available', async () => {
    const store = useReservationStore()
    await store.reserve(7, 3, 4)

    // Counter-check: as long as the server reports the seat as reserved, nothing happens.
    store.syncWithSeatMap(seatMapWithMySeat('RESERVED'))
    expect(store.reservation).not.toBeNull()

    store.syncWithSeatMap(seatMapWithMySeat('AVAILABLE'))
    expect(store.reservation).toBeNull()
    expect(store.expired).toBe(true)
  })

  it('forgets the reservation when the server says it is no longer active', async () => {
    const store = useReservationStore()
    await store.reserve(7, 3, 4)
    vi.mocked(reserveSeat).mockRejectedValueOnce(
      new ApiError({title: PROBLEM_TITLES.reservationNotActive, status: 409}),
    )

    await expect(store.reserve(7, 3, 5)).rejects.toBeInstanceOf(ApiError)

    // The seat change was sent with the token of the current reservation.
    expect(reserveSeat).toHaveBeenLastCalledWith(7, {
      seatRow: 3,
      seatNumber: 5,
      reservationToken: 'token-1'
    })
    expect(store.reservation).toBeNull()
    expect(store.expired).toBe(true)
  })
})
