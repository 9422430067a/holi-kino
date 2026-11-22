package de.zahra.kino.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@WebMvcTest(FrontendRoutesController.class)
class FrontendRoutesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void frontendRoutesAreServedByIndexHtml() throws Exception {
        mockMvc.perform(get("/screenings/7")).andExpect(forwardedUrl("/index.html"));
        mockMvc.perform(get("/screenings/7/booking")).andExpect(forwardedUrl("/index.html"));
        mockMvc.perform(get("/confirmation")).andExpect(forwardedUrl("/index.html"));
    }
}