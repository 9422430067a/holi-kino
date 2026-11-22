<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {RouterLink} from 'vue-router'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import type {Seat} from '@/api/types'
import {useSeatMap} from '@/composables/useSeatMap'
import {useReservationStore} from '@/stores/reservation'
import ReservationCountdown from "@/components/ReservationCountdown.vue";

const props = defineProps<{ screeningId: number }>()
const store = useReservationStore()
const {seatMap, loadError, reload} = useSeatMap(() => props.screeningId)

const message = ref<string | null>(null)
const busy = ref(false)

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
        <RouterLink :to="{ name: 'booking', params: { id: screeningId } }" class="next">
          Weiter zu deinen Daten
        </RouterLink>
      </div>
    </template>
  </main>
</template>

<style scoped>
.page {
  max-width: 60rem;
  margin: 0 auto;
  padding: 1.5rem;
  font-family: system-ui, sans-serif;
}

.error {
  color: #b00020;
}

.screen {
  margin: 1rem auto 1.5rem;
  padding: 0.25rem;
  max-width: 30rem;
  text-align: center;
  border-bottom: 4px solid #999;
  color: #666;
}

.seat-map {
  overflow-x: auto;
}

.row {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-bottom: 0.25rem;
}

.row-label {
  width: 1.5rem;
  text-align: right;
  color: #666;
  font-size: 0.8rem;
}

.seat {
  width: 2rem;
  height: 2rem;
  border: 1px solid #999;
  border-radius: 0.4rem 0.4rem 0.2rem 0.2rem;
  font-size: 0.7rem;
  display: inline-block;
}

.available {
  background: #fff;
  cursor: pointer;
}

.available:hover, .available:focus-visible {
  border-color: #000;
}

.mine {
  background: #2e7d32;
  color: #fff;
  border-color: #1b5e20;
}

.reserved {
  background: #f9a825;
}

.booked {
  background: #9e9e9e;
  color: #fff;
}

.legend {
  display: flex;
  gap: 1rem;
  list-style: none;
  padding: 0;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.legend .seat {
  width: 1rem;
  height: 1rem;
  vertical-align: middle;
}

.summary {
  font-weight: 600;
}

.next {
  display: inline-block;
  margin-top: 0.5rem;
  padding: 0.5rem 1rem;
  background: #222;
  color: #fff;
  border-radius: 0.4rem;
  text-decoration: none;
}
</style>
