import { onMounted, onUnmounted } from 'vue'

/**
 * Calls a task repeatedly while the page is visible.
 * The next call is only scheduled after the previous one has finished,
 * so there is never more than one call at a time. Stops when the component is unmounted.
 */
export function usePolling(task: () => Promise<void>, intervalMs: number): void {
  let timer: ReturnType<typeof setTimeout> | undefined
  let stopped = false

  function scheduleNext(): void {
    clearTimeout(timer)
    timer = setTimeout(run, intervalMs)
  }

  async function run(): Promise<void> {
    if (document.visibilityState === 'hidden') return // paused, resumes in onVisibilityChange
    try {
      await task()
    } catch {
      // Keep polling even if one call fails; the task reports its own errors.
    } finally {
      if (!stopped) scheduleNext()
    }
  }

  // Tab back in front: run right away, then continue the normal rhythm.
  function onVisibilityChange(): void {
    if (document.visibilityState === 'visible' && !stopped) {
      clearTimeout(timer)
      run()
    }
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', onVisibilityChange)
    scheduleNext()
  })

  onUnmounted(() => {
    stopped = true
    clearTimeout(timer)
    document.removeEventListener('visibilitychange', onVisibilityChange)
  })
}
