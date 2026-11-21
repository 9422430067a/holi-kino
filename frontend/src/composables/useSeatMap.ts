import { ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
import { ApiError } from '@/api/client'
import { fetchSeatMap } from '@/api/kinoApi'
import type { SeatMap } from '@/api/types'
import { usePolling } from './usePolling'

const POLL_INTERVAL_MS = 5000

export type SeatMapLoadError = 'not-bookable' | 'failed'

/**
 * Loads the seat map of a screening and keeps it up to date by polling.
 * Only the answer to the most recent request is shown, so a late answer cannot overwrite a newer state.
 */
export function useSeatMap(screeningId: MaybeRefOrGetter<number>) {
  const seatMap = ref<SeatMap | null>(null)
  const loadError = ref<SeatMapLoadError | null>(null)
  let latestRequest = 0

  async function reload(): Promise<void> {
    const request = ++latestRequest
    try {
      const result = await fetchSeatMap(toValue(screeningId))
      if (request !== latestRequest) return // an older answer arrived late: ignore it
      seatMap.value = result
      loadError.value = null
    } catch (error) {
      if (request !== latestRequest) return
      if (error instanceof ApiError && error.problem.status === 404) {
        loadError.value = 'not-bookable'
      } else if (!seatMap.value) {
        loadError.value = 'failed'
      }
      // Otherwise keep showing the last seat map; the next poll tries again.
    }
  }

  // A different screening: start from scratch, so the old seat map is never shown for the new one.
  watch(
    () => toValue(screeningId),
    () => {
      seatMap.value = null
      loadError.value = null
      reload()
    },
    { immediate: true },
  )

  usePolling(reload, POLL_INTERVAL_MS)

  return { seatMap, loadError, reload }
}
