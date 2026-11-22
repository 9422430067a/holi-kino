<script setup lang="ts">
import {computed} from 'vue'
import {useField} from 'vee-validate'

const props = defineProps<{ name: string }>()

const {value, errorMessage, meta, handleBlur} = useField<boolean>(props.name)

const showError = computed(() => meta.touched && !!errorMessage.value)
const errorId = `${props.name}-error`
</script>

<template>
  <div class="checkbox">
    <input
      :id="name"
      v-model="value"
      type="checkbox"
      :name="name"
      :aria-invalid="showError"
      :aria-describedby="showError ? errorId : undefined"
      @blur="handleBlur"
    />
    <!-- The label text comes from the page, so it can contain links later (for example to the terms). -->
    <label :for="name">
      <slot/>
    </label>
    <p v-if="showError" :id="errorId" class="field-error">{{ errorMessage }}</p>
  </div>
</template>

<style scoped>
.checkbox {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.5rem;
  align-items: start;
  margin-bottom: 1rem;
}

.field-error {
  grid-column: 2;
  color: #b00020;
  font-size: 0.9rem;
  margin: 0;
}
</style>
