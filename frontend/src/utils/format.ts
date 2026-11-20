const TIME_ZONE = 'Europe/Berlin'

const dayFormat = new Intl.DateTimeFormat('de-DE', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: TIME_ZONE,
})

const timeFormat = new Intl.DateTimeFormat('de-DE', {
  hour: '2-digit',
  minute: '2-digit',
  timeZone: TIME_ZONE,
})

export function formatDay(isoInstant: string): string {
  return dayFormat.format(new Date(isoInstant))
}

export function formatTime(isoInstant: string): string {
  return timeFormat.format(new Date(isoInstant))
}
