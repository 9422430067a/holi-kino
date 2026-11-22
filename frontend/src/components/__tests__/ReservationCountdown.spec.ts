import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {nextTick} from 'vue'
import {mount} from '@vue/test-utils'
import ReservationCountdown from '../ReservationCountdown.vue'

describe('ReservationCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2036-03-02T12:00:00Z'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('counts down and highlights the last minute', async () => {
    const wrapper = mount(ReservationCountdown, {
      props: {deadline: '2036-03-02T12:05:00Z'},
    })

    expect(wrapper.text()).toContain('5:00')
    expect(wrapper.classes()).not.toContain('urgent')

    // Four minutes and one second later.
    await vi.advanceTimersByTimeAsync(4 * 60 * 1000 + 1000)
    await nextTick()

    expect(wrapper.text()).toContain('0:59')
    expect(wrapper.classes()).toContain('urgent')

    wrapper.unmount()
  })
})
