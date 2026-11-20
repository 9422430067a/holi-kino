package de.zahra.kino.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/screenings/{screeningId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDto reserve(@PathVariable Long screeningId,
                                  @RequestBody ReserveSeatRequest request) {
        return reservationService.reserve(
                screeningId, request.seatRow(), request.seatNumber(), request.reservationToken());
    }
}