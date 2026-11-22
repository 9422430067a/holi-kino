<script setup lang="ts">
import type {Screening} from '@/api/types'
import {formatDay, formatTime} from '@/utils/format'

defineProps<{ screening: Screening }>()
</script>

<template>
  <p class="screening-info">
    <strong>{{ screening.movieTitle }}</strong>
    <span>{{ formatDay(screening.startsAt) }}, {{ formatTime(screening.startsAt) }} Uhr</span>
    <span>{{ screening.durationMinutes }} Min.</span>
  </p>
</template>

<style scoped>
.screening-info {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs) var(--space-m);
  margin: 0 0 var(--space-m);
  color: var(--color-text-muted);
}

.screening-info strong {
  color: var(--color-text);
}
</style>
