import {createRouter, createWebHistory} from 'vue-router'
import ScreeningListView from '@/views/ScreeningListView.vue'
import SeatMapView from '@/views/SeatMapView.vue'
import BookingView from '@/views/BookingView.vue'
import ConfirmationView from '@/views/ConfirmationView.vue'
import {useReservationStore} from '@/stores/reservation'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'screenings', component: ScreeningListView},
    {
      path: '/screenings/:id',
      name: 'seatMap',
      component: SeatMapView,
      // URL parameters are always strings; the view gets a real number.
      props: (route) => ({screeningId: Number(route.params.id)}),
    },
    {
      path: '/screenings/:id/booking',
      name: 'booking',
      component: BookingView,
      props: (route) => ({screeningId: Number(route.params.id)}),
      // Only with a running reservation for exactly this screening; otherwise back to its seat map.
      beforeEnter: (to) => {
        const reservation = useReservationStore().reservation
        if (reservation?.screeningId !== Number(to.params.id)) {
          return {name: 'seatMap', params: {id: to.params.id}}
        }
      },
    },
    {
      path: '/confirmation',
      name: 'confirmation',
      component: ConfirmationView,
      beforeEnter: () => {
        if (!useReservationStore().booking) {
          return {name: 'screenings'}
        }
      },
    },
  ],
})

export default router
