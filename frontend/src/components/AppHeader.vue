<script setup lang="ts">
import {computed} from 'vue'
import {RouterLink, useRoute} from 'vue-router'

const route = useRoute()

const STEPS = ['Vorstellung wählen', 'Platz wählen', 'Daten eingeben'] as const

// Which step each page belongs to. The confirmation counts as "all steps done".
const STEP_BY_ROUTE: Record<string, number> = {
  screenings: 1,
  seatMap: 2,
  booking: 3,
  confirmation: 4,
}

const currentStep = computed(() => STEP_BY_ROUTE[String(route.name)] ?? 1)
</script>

<template>
  <header class="app-header">
    <div class="inner">
      <RouterLink to="/" class="brand">Holi Lichtspielhaus</RouterLink>
      <ol class="steps" aria-label="Schritte der Buchung">
        <li
          v-for="(label, index) in STEPS"
          :key="label"
          :class="{ done: index + 1 < currentStep, current: index + 1 === currentStep }"
          :aria-current="index + 1 === currentStep ? 'step' : undefined"
        >
          <span class="number" aria-hidden="true">{{
              index + 1 < currentStep ? '✓' : index + 1
            }}</span>
          <span class="label">{{ label }}</span>
        </li>
      </ol>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.inner {
  max-width: var(--content-width);
  margin: 0 auto;
  padding: var(--space-m);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-m);
}

.brand {
  font-weight: 700;
  font-size: var(--font-size-l);
  color: var(--color-primary);
  text-decoration: none;
}

.steps {
  display: flex;
  gap: var(--space-m);
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: var(--font-size-s);
}

.steps li {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  color: var(--color-text-muted);
}

.number {
  display: inline-grid;
  place-items: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  font-weight: 600;
}

.current {
  color: var(--color-text);
  font-weight: 600;
}

.current .number {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.done .number {
  background: var(--color-primary-soft);
  border-color: var(--color-primary-soft);
  color: var(--color-primary);
}

/* On narrow screens only the current step keeps its label. */
@media (max-width: 40rem) {
  .steps li:not(.current) .label {
    display: none;
  }
}
</style>
