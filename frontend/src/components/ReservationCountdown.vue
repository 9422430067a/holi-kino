<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref} from 'vue'

const props = defineProps<{ deadline: string }>()

const now = ref(Date.now())
let ticker: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  ticker = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => clearInterval(ticker))

const secondsLeft = computed(() =>
  Math.max(0, Math.ceil((new Date(props.deadline).getTime() - now.value) / 1000)),
)

const display = computed(() => {
  const minutes = Math.floor(secondsLeft.value / 60)
  const seconds = secondsLeft.value % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
})
</script>

<template>
  <p class="countdown" :class="{ urgent: secondsLeft <= 60 }">
    Dein Platz ist noch
    <time :datetime="deadline">{{ display }}</time>
    Minuten für dich reserviert.
  </p>
</template>

<style scoped>
.countdown {
  font-variant-numeric: tabular-nums;
}

.urgent {
  color: #b00020;
  font-weight: 600;
}
</style>
