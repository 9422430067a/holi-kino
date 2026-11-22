<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {RouterLink} from 'vue-router'
import {fetchScreenings} from '@/api/kinoApi'
import type {Screening} from '@/api/types'
import {formatDay, formatTime} from '@/utils/format'
import {useScreeningStore} from "@/stores/screening.ts";

const screenings = ref<Screening[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const screeningStore = useScreeningStore()

onMounted(async () => {
  try {
    screenings.value = await fetchScreenings()
  } catch {
    error.value = 'Die Vorstellungen konnten nicht geladen werden. Bitte versuche es später erneut.'
  } finally {
    loading.value = false
  }
})

// Group by day, like a printed cinema programme. The backend already sorts by start time.
const days = computed(() => {
  const groups = new Map<string, Screening[]>()
  for (const screening of screenings.value) {
    const day = formatDay(screening.startsAt)
    groups.set(day, [...(groups.get(day) ?? []), screening])
  }
  return [...groups.entries()].map(([day, items]) => ({day, items}))
})
</script>

<template>
  <main class="page">
    <h1>Vorstellungen</h1>
    <p class="lead">Wähle eine Vorstellung.</p>

    <p v-if="loading">Vorstellungen werden geladen …</p>
    <p v-else-if="error" class="error" role="alert">{{ error }}</p>
    <p v-else-if="days.length === 0">Zurzeit sind keine Vorstellungen geplant.</p>

    <template v-else>
      <section v-for="group in days" :key="group.day" class="day">
        <h2>{{ group.day }}</h2>
        <ul>
          <li v-for="screening in group.items" :key="screening.id">
            <RouterLink :to="{ name: 'seatMap', params: { id: screening.id } }"
                        class="screening"
                        @click="screeningStore.select(screening)"
            >
              <span class="time">{{ formatTime(screening.startsAt) }}</span>
              <span class="title">{{ screening.movieTitle }}</span>
              <span class="duration">{{ screening.durationMinutes }} Min.</span>
            </RouterLink>
          </li>
        </ul>
      </section>
    </template>
  </main>
</template>

<style scoped>
.page {
  max-width: 40rem;
  margin: 0 auto;
  padding: 1.5rem;
  font-family: system-ui, sans-serif;
}

.lead {
  color: #555;
}

.error {
  color: #b00020;
}

.day h2 {
  font-size: 1.1rem;
  margin-top: 1.5rem;
}

ul {
  list-style: none;
  padding: 0;
}

.screening {
  display: grid;
  grid-template-columns: 4rem 1fr auto;
  gap: 1rem;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  color: inherit;
  text-decoration: none;
}

.screening:hover, .screening:focus-visible {
  border-color: #333;
}

.time {
  font-weight: 600;
}

.duration {
  color: #555;
}
</style>
