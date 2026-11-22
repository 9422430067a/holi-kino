import {defineStore} from 'pinia'
import type {Screening} from '@/api/types'

// The screening the guest is currently looking at.
export const useScreeningStore = defineStore('screening', {
  state: () => ({
    selected: null as Screening | null,
  }),

  actions: {
    select(screening: Screening): void {
      this.selected = screening
    },
  },
})
