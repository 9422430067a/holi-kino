package de.zahra.kino.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class ReservationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidSeatException.class)
    public ProblemDetail handleInvalidSeat(InvalidSeatException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ScreeningNotAvailableException.class)
    public ProblemDetail handleScreeningNotAvailable(ScreeningNotAvailableException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(SeatAlreadyTakenException.class)
    public ProblemDetail handleSeatAlreadyTaken(SeatAlreadyTakenException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Seat already taken");
        return problem;
    }

    @ExceptionHandler(ReservationNotActiveException.class)
    public ProblemDetail handleReservationNotActive(ReservationNotActiveException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Reservation no longer active");
        return problem;
    }
}