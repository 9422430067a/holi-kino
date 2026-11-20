<script setup lang="ts">
defineProps<{ screeningId: number }>()
</script>

<template>
  <main>
    <p>Sitzplan für Vorstellung {{ screeningId }} folgt.</p>
  </main>
</template>
