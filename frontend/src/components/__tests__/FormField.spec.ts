import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {defineComponent, h} from 'vue'
import {mount} from '@vue/test-utils'
import {useForm} from 'vee-validate'
import {validateEmail} from '@/utils/bookingRules'
import FormField from '../FormField.vue'

// FormField needs a surrounding useForm, so the test renders it inside a tiny host form.
const HostForm = defineComponent({
  setup() {
    useForm({initialValues: {email: ''}, validationSchema: {email: validateEmail}})
    return () => h(FormField, {name: 'email', label: 'E-Mail-Adresse', type: 'email'})
  },
})

// VeeValidate runs its validation slightly delayed (debounced). In a test no time passes on its own,
// so we move the fake clock past that delay; this also runs all pending promises.
async function letValidationFinish(): Promise<void> {
  await vi.advanceTimersByTimeAsync(50)
}

describe('FormField', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    // Fake timers affect everything that runs afterwards; restore the real clock so later tests are not stuck.
    vi.useRealTimers()
  })

  it('shows the error only after the guest leaves the field', async () => {
    const wrapper = mount(HostForm)
    const input = wrapper.find('input')

    await input.setValue('keine-mail')
    await letValidationFinish()

    // While typing: no message yet, and the field is not marked as invalid.
    expect(wrapper.find('.field-error').exists()).toBe(false)
    expect(input.attributes('aria-invalid')).not.toBe('true')

    await input.trigger('blur')
    await letValidationFinish()

    // After leaving the field: message shown and connected to the input for screen readers.
    expect(wrapper.find('.field-error').text()).toBe('Bitte gib eine gültige E-Mail-Adresse ein.')
    expect(input.attributes('aria-invalid')).toBe('true')
    expect(input.attributes('aria-describedby')).toBe('email-error')
    expect(wrapper.find('#email-error').exists()).toBe(true)

    wrapper.unmount()
  })
})
