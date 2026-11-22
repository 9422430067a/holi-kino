<script setup lang="ts">
import {RouterLink, useRouter} from 'vue-router'
import CheckboxField from '@/components/CheckboxField.vue'
import FormField from '@/components/FormField.vue'
import ReservationCountdown from '@/components/ReservationCountdown.vue'
import {useBookingForm} from '@/composables/useBookingForm'
import {useReservationStore} from '@/stores/reservation'
import {todayAsIsoDate} from '@/utils/bookingRules'
import {useScreening} from "@/composables/useScreening.ts";
import ScreeningInfo from "@/components/ScreeningInfo.vue";

const props = defineProps<{ screeningId: number }>()
const store = useReservationStore()
const router = useRouter()

const {screening} = useScreening(() => props.screeningId)

const {onSubmit, isSubmitting, submitError} = useBookingForm({
  onBooked: () => router.push({name: 'confirmation'}),
})
</script>

<template>
  <main class="page">
    <RouterLink :to="{ name: 'seatMap', params: { id: screeningId } }">← Zurück zum Sitzplan
    </RouterLink>
    <h1>Deine Daten</h1>
    <ScreeningInfo v-if="screening" :screening="screening"/>

    <div v-if="store.expired" class="expired" role="alert">
      <p>Deine Reservierung ist abgelaufen. Bitte wähle einen neuen Platz.</p>
      <RouterLink :to="{ name: 'seatMap', params: { id: screeningId } }" class="button">
        Neuen Platz wählen
      </RouterLink>
    </div>

    <div v-if="store.reservation" class="summary">
      <p>Dein Platz: Reihe {{ store.reservation.seatRow }}, Platz {{
          store.reservation.seatNumber
        }}</p>
      <ReservationCountdown :deadline="store.reservation.reservedUntil"/>
    </div>

    <form novalidate @submit="onSubmit">
      <fieldset :disabled="!store.reservation || isSubmitting">
        <p class="hint">Felder mit * sind Pflichtfelder.</p>

        <FormField name="fullName" label="Name *" type="text" autocomplete="name"/>
        <FormField name="birthDate" label="Geburtsdatum *" type="date" autocomplete="bday"
                   :max="todayAsIsoDate()"/>
        <FormField name="email" label="E-Mail-Adresse *" type="email" autocomplete="email"/>
        <FormField name="phone" label="Telefonnummer (optional)" type="tel" autocomplete="tel"/>

        <CheckboxField name="termsAccepted">Ich akzeptiere die AGB. *</CheckboxField>
        <CheckboxField name="privacyAccepted">
          Ich habe die Datenschutzerklärung gelesen und stimme ihr zu. *
        </CheckboxField>
        <CheckboxField name="newsletterAccepted">Ich möchte den Newsletter erhalten (optional).
        </CheckboxField>

        <p v-if="submitError" class="error" role="alert">{{ submitError }}</p>

        <button type="submit" class="button">
          {{ isSubmitting ? 'Wird gebucht …' : 'Verbindlich buchen' }}
        </button>
      </fieldset>
    </form>
  </main>
</template>

<style scoped>
.page {
  max-width: 40rem;
  margin: 0 auto;
  padding: 1.5rem;
  font-family: system-ui, sans-serif;
}

.summary p {
  margin: 0.25rem 0;
  font-weight: 600;
}

.expired {
  padding: 1rem;
  margin: 1rem 0;
  border: 1px solid #b00020;
  border-radius: 0.5rem;
  color: #b00020;
}

fieldset {
  border: none;
  padding: 0;
  margin: 1rem 0 0;
}

fieldset:disabled {
  opacity: 0.5;
}

.hint {
  color: #555;
  font-size: 0.9rem;
}

.error {
  color: #b00020;
}

.button {
  display: inline-block;
  padding: 0.6rem 1.2rem;
  background: #222;
  color: #fff;
  border: none;
  border-radius: 0.4rem;
  font: inherit;
  text-decoration: none;
  cursor: pointer;
}
</style>
