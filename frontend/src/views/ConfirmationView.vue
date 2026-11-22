<script setup lang="ts">
import {RouterLink} from 'vue-router'
import {useReservationStore} from '@/stores/reservation'
import ScreeningInfo from "@/components/ScreeningInfo.vue";

const store = useReservationStore()
</script>

<template>
  <main v-if="store.booking" class="page">
    <div class="confirmation">
      <p class="check" aria-hidden="true">✓</p>
      <h1>Deine Buchung ist bestätigt</h1>
      <ScreeningInfo v-if="store.bookedScreening" :screening="store.bookedScreening"/>
      <p class="seat">Reihe {{ store.booking.seatRow }}, Platz {{ store.booking.seatNumber }}</p>
      <p>Buchungsnummer: <code>{{ store.booking.bookingId }}</code></p>
      <p class="muted">Bitte nenne diese Buchungsnummer an der Kasse.</p>
      <RouterLink to="/" class="button">Zurück zu allen Vorstellungen</RouterLink>
    </div>
  </main>
</template>

<style scoped>
.confirmation {
  padding: var(--space-xl) var(--space-l);
  text-align: center;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-m);
}

.check {
  display: inline-grid;
  place-items: center;
  width: 3rem;
  height: 3rem;
  margin: 0 auto var(--space-m);
  border-radius: 50%;
  font-size: var(--font-size-l);
  font-weight: 700;
  color: var(--color-primary-contrast);
  background: var(--color-primary);
}

.seat {
  font-size: var(--font-size-l);
  font-weight: 700;
}

code {
  padding: var(--space-xs) var(--space-s);
  background: var(--color-bg);
  border-radius: var(--radius-s);
  word-break: break-all;
}

.confirmation :deep(.screening-info) {
  justify-content: center;
}
</style>
