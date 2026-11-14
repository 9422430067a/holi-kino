package de.zahra.kino.screening;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final Clock clock;

    public ScreeningService(ScreeningRepository screeningRepository, Clock clock) {
        this.screeningRepository = screeningRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ScreeningDto> findUpcomingScreenings() {
        return screeningRepository.findUpcomingWithMovie(Instant.now(clock))
                .stream()
                .map(ScreeningDto::from)
                .toList();
    }
}