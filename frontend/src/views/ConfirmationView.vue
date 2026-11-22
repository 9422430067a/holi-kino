<script setup lang="ts">
import {RouterLink} from 'vue-router'
import {useReservationStore} from '@/stores/reservation'
import ScreeningInfo from "@/components/ScreeningInfo.vue";

const store = useReservationStore()
</script>

<template>
  <main v-if="store.booking" class="page">
    <h1>Deine Buchung ist bestätigt</h1>
    <ScreeningInfo v-if="store.bookedScreening" :screening="store.bookedScreening"/>
    <p>
      Reihe {{ store.booking.seatRow }}, Platz {{ store.booking.seatNumber }}
    </p>
    <p>
      Buchungsnummer: <code>{{ store.booking.bookingId }}</code>
    </p>
    <p class="hint">Bitte nenne diese Buchungsnummer an der Kasse.</p>
    <RouterLink to="/">Zurück zu allen Vorstellungen</RouterLink>
  </main>
</template>

<style scoped>
.page {
  max-width: 40rem;
  margin: 0 auto;
  padding: 1.5rem;
  font-family: system-ui, sans-serif;
}

.hint {
  color: #555;
}
</style>
