package de.zahra.kino.screening;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScreeningController.class)
class ScreeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @Test
    void unknownScreeningReturns404() throws Exception {
        when(screeningService.getScreening(7L)).thenThrow(new ScreeningNotFoundException(7L));

        mockMvc.perform(get("/api/screenings/7"))
                .andExpect(status().isNotFound());
    }
}