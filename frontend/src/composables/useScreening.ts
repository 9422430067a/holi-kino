import {computed, type MaybeRefOrGetter, toValue, watch} from 'vue'
import {fetchScreening} from '@/api/kinoApi'
import {useScreeningStore} from '@/stores/screening'

/**
 * The screening with the given id: taken from the store if the guest came from the list,
 * otherwise loaded from the backend (direct link, new tab, reload).
 */
export function useScreening(screeningId: MaybeRefOrGetter<number>) {
  const store = useScreeningStore()

  const screening = computed(() =>
    store.selected?.id === toValue(screeningId) ? store.selected : null,
  )

  async function loadIfMissing(): Promise<void> {
    if (screening.value) return
    try {
      store.select(await fetchScreening(toValue(screeningId)))
    } catch {
      // Without title and time the page still works, so there is nothing else to do.
    }
  }

  watch(() => toValue(screeningId), loadIfMissing, {immediate: true})

  return {screening}
}
