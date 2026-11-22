<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {RouterLink} from 'vue-router'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import type {Seat} from '@/api/types'
import {useSeatMap} from '@/composables/useSeatMap'
import {useReservationStore} from '@/stores/reservation'
import ReservationCountdown from "@/components/ReservationCountdown.vue";
import {useScreening} from "@/composables/useScreening.ts";
import ScreeningInfo from "@/components/ScreeningInfo.vue";

const props = defineProps<{ screeningId: number }>()
const store = useReservationStore()
const {seatMap, loadError, reload} = useSeatMap(() => props.screeningId)

const message = ref<string | null>(null)
const busy = ref(false)

const {screening} = useScreening(() => props.screeningId)

const LOAD_ERROR_TEXT = {
  'not-bookable': 'Diese Vorstellung ist nicht mehr buchbar.',
  failed: 'Der Sitzplan konnte nicht geladen werden.',
} as const

const STATUS_TEXT = {AVAILABLE: 'frei', RESERVED: 'reserviert', BOOKED: 'gebucht'} as const

// The backend reports the guest's own seat as RESERVED; the store knows which one it is.
function isMine(seatRow: number, seat: Seat): boolean {
  const reservation = store.reservation
  return (
    reservation !== null &&
    reservation.screeningId === props.screeningId &&
    reservation.seatRow === seatRow &&
    reservation.seatNumber === seat.seatNumber
  )
}

const myReservationHere = computed(() =>
  store.reservation?.screeningId === props.screeningId ? store.reservation : null,
)

async function selectSeat(seatRow: number, seat: Seat): Promise<void> {
  if (busy.value || isMine(seatRow, seat) || seat.status !== 'AVAILABLE') return
  busy.value = true
  message.value = null
  try {
    await store.reserve(props.screeningId, seatRow, seat.seatNumber)
  } catch (error) {
    message.value = messageFor(error)
  } finally {
    // Success or not: show the current state of the auditorium.
    await reload()
    busy.value = false
  }
}

function messageFor(error: unknown): string | null {
  if (error instanceof ApiError) {
    if (error.problem.title === PROBLEM_TITLES.seatAlreadyTaken) {
      return 'Dieser Platz wurde gerade von jemand anderem gewählt. Bitte wähle einen anderen.'
    }
    if (error.problem.title === PROBLEM_TITLES.reservationNotActive) {
      return null // shown through store.expired
    }
    if (error.problem.status === 404) {
      return 'Diese Vorstellung ist nicht mehr buchbar.'
    }
  }
  return 'Das hat leider nicht geklappt. Bitte versuche es erneut.'
}

function seatLabel(seatRow: number, seat: Seat): string {
  const state = isMine(seatRow, seat) ? 'dein Platz' : STATUS_TEXT[seat.status]
  return `Reihe ${seatRow}, Platz ${seat.seatNumber}, ${state}`
}

// After every answer from the server: is my reservation still valid?
watch(seatMap, (value) => {
  if (value) store.syncWithSeatMap(value)
})
</script>

<template>
  <main class="page">
    <RouterLink to="/">← Alle Vorstellungen</RouterLink>
    <h1>Platz wählen</h1>
    <ScreeningInfo v-if="screening" :screening="screening"/>

    <p v-if="loadError" class="error" role="alert">{{ LOAD_ERROR_TEXT[loadError] }}</p>
    <p v-else-if="!seatMap">Sitzplan wird geladen …</p>

    <template v-else>
      <div class="screen" aria-hidden="true">Leinwand</div>

      <div class="seat-map">
        <div v-for="row in seatMap.rows" :key="row.seatRow" class="row">
          <span class="row-label" aria-hidden="true">{{ row.seatRow }}</span>
          <button
            v-for="seat in row.seats"
            :key="seat.seatNumber"
            type="button"
            class="seat"
            :class="isMine(row.seatRow, seat) ? 'mine' : seat.status.toLowerCase()"
            :disabled="busy || (seat.status !== 'AVAILABLE' && !isMine(row.seatRow, seat))"
            :aria-pressed="isMine(row.seatRow, seat)"
            :aria-label="seatLabel(row.seatRow, seat)"
            @click="selectSeat(row.seatRow, seat)"
          >
            {{ seat.seatNumber }}
          </button>
        </div>
      </div>

      <ul class="legend">
        <li><span class="seat available"></span> frei</li>
        <li><span class="seat mine"></span> dein Platz</li>
        <li><span class="seat reserved"></span> reserviert</li>
        <li><span class="seat booked"></span> gebucht</li>
      </ul>

      <p v-if="message" class="error" role="alert">{{ message }}</p>

      <p v-if="store.expired" class="error" role="alert">
        Deine Reservierung ist abgelaufen. Bitte wähle erneut einen Platz.
      </p>

      <div v-if="myReservationHere" class="summary">
        <p>Dein Platz: Reihe {{ myReservationHere.seatRow }}, Platz {{
            myReservationHere.seatNumber
          }}</p>
        <ReservationCountdown :deadline="myReservationHere.reservedUntil"/>
        <RouterLink :to="{ name: 'booking', params: { id: screeningId } }" class="button">
          Weiter zu deinen Daten
        </RouterLink>
      </div>
    </template>
  </main>
</template>

<style scoped>
.screen {
  margin: var(--space-l) auto var(--space-l);
  padding: var(--space-xs);
  max-width: 30rem;
  text-align: center;
  font-size: var(--font-size-s);
  color: var(--color-text-muted);
  border-bottom: 4px solid var(--color-border);
  border-radius: 0 0 50% 50% / 0 0 100% 100%;
}

.seat-map {
  overflow-x: auto;
  padding-bottom: var(--space-s);
}

.row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  margin-bottom: var(--space-xs);
  min-width: max-content;
}

.row-label {
  width: 1.5rem;
  text-align: right;
  color: var(--color-text-muted);
  font-size: var(--font-size-s);
}

.seat {
  width: 2rem;
  height: 2rem;
  padding: 0;
  font-size: 0.7rem;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-s) var(--radius-s) 2px 2px;
}

.available {
  background: var(--color-seat-available);
  cursor: pointer;
}

.available:hover {
  border-color: var(--color-primary);
}

.mine {
  background: var(--color-seat-mine);
  border-color: var(--color-seat-mine);
  color: var(--color-primary-contrast);
  font-weight: 700;
}

.reserved {
  background: var(--color-seat-reserved);
}

.booked {
  background: var(--color-seat-booked);
}

.legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-m);
  list-style: none;
  padding: 0;
  margin: var(--space-m) 0;
  font-size: var(--font-size-s);
}

.legend .seat {
  width: 1rem;
  height: 1rem;
  display: inline-block;
  vertical-align: middle;
}

.summary {
  margin-top: var(--space-l);
  padding: var(--space-m);
  background: var(--color-primary-soft);
  border-radius: var(--radius-m);
}

.summary p {
  margin: 0 0 var(--space-xs);
  font-weight: 600;
}
</style>
