import {describe, expect, it} from 'vitest'
import {
  todayAsIsoDate,
  validateBirthDate,
  validateEmail,
  validateFullName,
  validateTermsAccepted,
} from '../bookingRules'

describe('validateFullName', () => {
  it('accepts a name', () => {
    expect(validateFullName('Ada Lovelace')).toBe(true)
  })

  it('rejects a name made of spaces only', () => {
    expect(validateFullName('   ')).toBe('Bitte gib deinen Namen ein.')
  })
})

describe('validateEmail', () => {
  it('accepts a normal address', () => {
    expect(validateEmail('ada@example.com')).toBe(true)
  })

  it('rejects text without an @', () => {
    expect(validateEmail('keine-mail')).toBe('Bitte gib eine gültige E-Mail-Adresse ein.')
  })
})

describe('validateBirthDate', () => {
  it('accepts a date in the past', () => {
    expect(validateBirthDate('1990-05-17')).toBe(true)
  })

  it('rejects today, like @Past in the backend', () => {
    expect(validateBirthDate(todayAsIsoDate())).toBe('Das Geburtsdatum muss in der Vergangenheit liegen.')
  })
})

describe('validateTermsAccepted', () => {
  it('requires the consent', () => {
    expect(validateTermsAccepted(true)).toBe(true)
    expect(validateTermsAccepted(false)).toBe('Bitte stimme den AGB zu.')
  })
})
