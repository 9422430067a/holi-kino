import {describe, expect, it} from 'vitest'
import {formatDay, formatTime} from '../format'

describe('date and time formatting', () => {
  it('shows the cinema time in Berlin, not the time in UTC', () => {
    // 23:30 UTC on 2 March is already 00:30 on 3 March in Berlin (winter time, UTC+1).
    const startsAt = '2036-03-02T23:30:00Z'

    expect(formatTime(startsAt)).toBe('00:30')
    expect(formatDay(startsAt)).toBe('Montag, 3. März 2036')
  })
})
