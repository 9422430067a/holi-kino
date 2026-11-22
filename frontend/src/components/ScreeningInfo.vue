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
  gap: 0.25rem 1rem;
  color: #333;
}
</style>
