// The same rules as BookSeatRequest in the backend. Here they are for comfort, there they are required.
// Each rule returns true when the value is fine, otherwise the message to show.

export type RuleResult = true | string

// Deliberately as lenient as the backend's @Email: it only catches obvious typos.
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+$/

// Today as "YYYY-MM-DD" in the guest's local time, the same format as <input type="date">.
export function todayAsIsoDate(): string {
  const today = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
}

export function validateFullName(value: string): RuleResult {
  if (!value.trim()) return 'Bitte gib deinen Namen ein.'
  return value.trim().length <= 200 || 'Der Name darf höchstens 200 Zeichen lang sein.'
}

export function validateBirthDate(value: string): RuleResult {
  if (!value) return 'Bitte gib dein Geburtsdatum ein.'
  return value < todayAsIsoDate() || 'Das Geburtsdatum muss in der Vergangenheit liegen.'
}

export function validateEmail(value: string): RuleResult {
  if (!value.trim()) return 'Bitte gib deine E-Mail-Adresse ein.'
  if (!EMAIL_PATTERN.test(value.trim())) return 'Bitte gib eine gültige E-Mail-Adresse ein.'
  return value.trim().length <= 254 || 'Die E-Mail-Adresse ist zu lang.'
}

export function validatePhone(value: string): RuleResult {
  return value.trim().length <= 50 || 'Die Telefonnummer darf höchstens 50 Zeichen lang sein.'
}

export function validateTermsAccepted(value: boolean): RuleResult {
  return value || 'Bitte stimme den AGB zu.'
}

export function validatePrivacyAccepted(value: boolean): RuleResult {
  return value || 'Bitte stimme der Datenschutzerklärung zu.'
}
