package de.zahra.kino.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void successfulReservationReturns201WithTokenAndDeadline() throws Exception {
        UUID token = UUID.randomUUID();
        when(reservationService.reserve(7L, 3, 4, null))
                .thenReturn(new ReservationDto(token, 7L, 3, 4, Instant.parse("2036-01-01T12:05:00Z")));

        reserveSeat()
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationToken").value(token.toString()))
                .andExpect(jsonPath("$.reservedUntil").value("2036-01-01T12:05:00Z"));
    }

    @Test
    void seatOutsideTheAuditoriumReturns400() throws Exception {
        when(reservationService.reserve(7L, 3, 4, null)).thenThrow(new InvalidSeatException(3, 4));

        reserveSeat()
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unavailableScreeningReturns404() throws Exception {
        when(reservationService.reserve(7L, 3, 4, null)).thenThrow(new ScreeningNotAvailableException(7L));

        reserveSeat()
                .andExpect(status().isNotFound());
    }

    @Test
    void takenSeatReturns409WithItsOwnTitle() throws Exception {
        when(reservationService.reserve(7L, 3, 4, null)).thenThrow(new SeatAlreadyTakenException(7L, 3, 4));

        reserveSeat()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Seat already taken"));
    }

    @Test
    void inactiveReservationReturns409WithItsOwnTitle() throws Exception {
        when(reservationService.reserve(7L, 3, 4, null)).thenThrow(new ReservationNotActiveException(UUID.randomUUID()));

        reserveSeat()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reservation no longer active"));
    }

    private ResultActions reserveSeat() throws Exception {
        return mockMvc.perform(post("/api/screenings/7/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "seatRow": 3, "seatNumber": 4 }
                        """));
    }
}