package de.zahra.kino.screening;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping
    public List<ScreeningDto> getUpcomingScreenings() {
        return screeningService.findUpcomingScreenings();
    }

    @GetMapping("/{id}")
    public ScreeningDto getScreening(@PathVariable Long id) {
        return screeningService.getScreening(id);
    }
}