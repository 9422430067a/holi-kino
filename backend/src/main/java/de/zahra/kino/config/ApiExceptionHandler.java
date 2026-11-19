package de.zahra.kino.config;

import de.zahra.kino.reservation.InvalidSeatException;
import de.zahra.kino.reservation.ReservationNotActiveException;
import de.zahra.kino.reservation.ScreeningNotAvailableException;
import de.zahra.kino.reservation.SeatAlreadyTakenException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        // One message per field, so the frontend can show it next to the input.
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = exception.getBody();
        problem.setTitle("Invalid request data");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }
}