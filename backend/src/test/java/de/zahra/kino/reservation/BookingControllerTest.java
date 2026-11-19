package de.zahra.kino.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String TOKEN = "3f2a6c1e-8b4d-4f7a-9c2e-1d5b7a9e0f11";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void validBookingReturns201() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(bookingService.book(any(BookSeatRequest.class)))
                .thenReturn(new BookingDto(bookingId, 7L, 3, 4));

        book(validBody())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
                .andExpect(jsonPath("$.seatRow").value(3))
                .andExpect(jsonPath("$.seatNumber").value(4));
    }

    @Test
    void invalidFieldsReturn400WithOneErrorPerField() throws Exception {
        book("""
                {
                  "reservationToken": "%s",
                  "fullName": "   ",
                  "birthDate": "2090-01-01",
                  "email": "keine-mail",
                  "termsAccepted": false,
                  "privacyAccepted": true,
                  "newsletterAccepted": false
                }
                """.formatted(TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request data"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.birthDate").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.termsAccepted").exists())
                .andExpect(jsonPath("$.errors.privacyAccepted").doesNotExist())
                .andExpect(jsonPath("$.errors.phone").doesNotExist());

        verifyNoInteractions(bookingService);
    }

    @Test
    void missingConsentIsRejected() throws Exception {
        book("""
                {
                  "reservationToken": "%s",
                  "fullName": "Ada Lovelace",
                  "birthDate": "1990-05-17",
                  "email": "ada@example.com"
                }
                """.formatted(TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.termsAccepted").exists())
                .andExpect(jsonPath("$.errors.privacyAccepted").exists());

        verifyNoInteractions(bookingService);
    }

    @Test
    void unreadableJsonReturns400WithoutFieldErrors() throws Exception {
        book(validBody().replace("1990-05-17", "1990-13-45"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").doesNotExist());

        verifyNoInteractions(bookingService);
    }

    @Test
    void inactiveReservationReturns409() throws Exception {
        when(bookingService.book(any(BookSeatRequest.class)))
                .thenThrow(new ReservationNotActiveException(UUID.fromString(TOKEN)));

        book(validBody())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reservation no longer active"));
    }

    @Test
    void newsletterMayBeLeftOut() throws Exception {
        when(bookingService.book(any(BookSeatRequest.class)))
                .thenReturn(new BookingDto(UUID.randomUUID(), 7L, 3, 4));

        book("""
                {
                  "reservationToken": "%s",
                  "fullName": "Ada Lovelace",
                  "birthDate": "1990-05-17",
                  "email": "ada@example.com",
                  "termsAccepted": true,
                  "privacyAccepted": true
                }
                """.formatted(TOKEN))
                .andExpect(status().isCreated());
    }

    private ResultActions book(String body) throws Exception {
        return mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String validBody() {
        return """
                {
                  "reservationToken": "%s",
                  "fullName": "Ada Lovelace",
                  "birthDate": "1990-05-17",
                  "email": "ada@example.com",
                  "phone": null,
                  "termsAccepted": true,
                  "privacyAccepted": true,
                  "newsletterAccepted": false
                }
                """.formatted(TOKEN);
    }
}