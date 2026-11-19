import { createRouter, createWebHistory } from 'vue-router'
import ScreeningListView from '@/views/ScreeningListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'screenings', component: ScreeningListView },
  ],
})

export default router
