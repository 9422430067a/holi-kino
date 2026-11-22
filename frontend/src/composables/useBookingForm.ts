import {ref} from 'vue'
import {useForm} from 'vee-validate'
import {ApiError, PROBLEM_TITLES} from '@/api/client'
import {useReservationStore} from '@/stores/reservation'
import {
  validateBirthDate,
  validateEmail,
  validateFullName,
  validatePhone,
  validatePrivacyAccepted,
  validateTermsAccepted,
} from '@/utils/bookingRules'

export interface BookingFormFields {
  fullName: string
  birthDate: string
  email: string
  phone: string
  termsAccepted: boolean
  privacyAccepted: boolean
  newsletterAccepted: boolean
}

interface Options {
  // Called after a successful booking; the page decides where to go next.
  // The result is not used, the form only waits for it to finish.
  onBooked: () => unknown
}

/**
 * The booking form: validation, submitting, and showing server errors next to the fields.
 * The fields themselves register through useField (see FormField and CheckboxField).
 */
export function useBookingForm({onBooked}: Options) {
  const store = useReservationStore()
  const submitError = ref<string | null>(null)

  const {handleSubmit, isSubmitting, setErrors} = useForm<BookingFormFields>({
    initialValues: {
      fullName: '',
      birthDate: '',
      email: '',
      phone: '',
      termsAccepted: false,
      privacyAccepted: false,
      newsletterAccepted: false,
    },
    validationSchema: {
      fullName: validateFullName,
      birthDate: validateBirthDate,
      email: validateEmail,
      phone: validatePhone,
      termsAccepted: validateTermsAccepted,
      privacyAccepted: validatePrivacyAccepted,
    },
  })

  async function submitBooking(values: BookingFormFields): Promise<void> {
    submitError.value = null
    try {
      await store.book({
        fullName: values.fullName.trim(),
        birthDate: values.birthDate,
        email: values.email.trim(),
        phone: values.phone.trim() || undefined,
        termsAccepted: values.termsAccepted,
        privacyAccepted: values.privacyAccepted,
        newsletterAccepted: values.newsletterAccepted,
      })
      await onBooked()
    } catch (error) {
      if (error instanceof ApiError) {
        // Field errors from the server appear exactly like our own ones.
        if (error.problem.errors) {
          setErrors(error.problem.errors)
          return
        }
        if (error.problem.title === PROBLEM_TITLES.reservationNotActive) {
          return // the store has set "expired", the page shows it
        }
      }
      submitError.value = 'Die Buchung hat leider nicht geklappt. Bitte versuche es erneut.'
    }
  }

  // Validates all fields first and only calls submitBooking if everything is fine.
  const onSubmit = handleSubmit(submitBooking)

  return {onSubmit, isSubmitting, submitError}
}
