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
  gap: var(--space-s);
  align-items: start;
  margin-bottom: var(--space-m);
}

input {
  width: 1.1rem;
  height: 1.1rem;
  margin-top: 0.2rem;
  accent-color: var(--color-primary);
}

.field-error {
  grid-column: 2;
  margin: 0;
  font-size: var(--font-size-s);
  color: var(--color-danger);
}
</style>
