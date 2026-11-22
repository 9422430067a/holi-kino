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
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.25rem;
}

input {
  width: 100%;
  padding: 0.5rem;
  font: inherit;
  box-sizing: border-box;
}

input[aria-invalid='true'] {
  border-color: #b00020;
}

.field-error {
  color: #b00020;
  font-size: 0.9rem;
  margin: 0.25rem 0 0;
}
</style>
