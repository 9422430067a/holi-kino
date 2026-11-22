<script setup lang="ts">
import {computed} from 'vue'
import {useField} from 'vee-validate'

// type, autocomplete, max and other input attributes are passed straight to the <input>.
defineOptions({inheritAttrs: false})

const props = defineProps<{ name: string; label: string }>()

// Registers this field with the surrounding useForm, which holds the value and the rules.
const {value, errorMessage, meta, handleBlur} = useField<string>(props.name)

// Show an error only after the guest has left the field (or submitted), not while typing.
const showError = computed(() => meta.touched && !!errorMessage.value)
const errorId = `${props.name}-error`
</script>

<template>
  <div class="field">
    <label :for="name">{{ label }}</label>
    <input
      :id="name"
      v-model="value"
      v-bind="$attrs"
      :name="name"
      :aria-invalid="showError"
      :aria-describedby="showError ? errorId : undefined"
      @blur="handleBlur"
    />
    <p v-if="showError" :id="errorId" class="field-error">{{ errorMessage }}</p>
  </div>
</template>

<style scoped>
.field {
  margin-bottom: var(--space-m);
}

label {
  display: block;
  margin-bottom: var(--space-xs);
  font-weight: 600;
}

input {
  width: 100%;
  padding: var(--space-s) var(--space-m);
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-s);
}

input[aria-invalid='true'] {
  border-color: var(--color-danger);
  background: var(--color-danger-soft);
}

.field-error {
  margin: var(--space-xs) 0 0;
  font-size: var(--font-size-s);
  color: var(--color-danger);
}
</style>
