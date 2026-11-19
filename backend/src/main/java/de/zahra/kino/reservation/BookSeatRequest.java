package de.zahra.kino.reservation;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record BookSeatRequest(
        @NotNull
        UUID reservationToken,

        @NotBlank @Size(max = 200)
        String fullName,

        @NotNull @Past
        LocalDate birthDate,

        @NotBlank @Email @Size(max = 254)
        String email,

        @Size(max = 50)
        String phone,

        @NotNull @AssertTrue
        Boolean termsAccepted,

        @NotNull @AssertTrue
        Boolean privacyAccepted,

        Boolean newsletterAccepted
) {
}