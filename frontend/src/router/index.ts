import {createRouter, createWebHistory} from 'vue-router'
import ScreeningListView from '@/views/ScreeningListView.vue'
import SeatMapView from "@/views/SeatMapView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'screenings', component: ScreeningListView},
    {
      path: '/screenings/:id',
      name: 'seatMap',
      component: SeatMapView,
      props: (route) => ({screeningId: Number(route.params.id)}),
    },
  ],
})

export default router
