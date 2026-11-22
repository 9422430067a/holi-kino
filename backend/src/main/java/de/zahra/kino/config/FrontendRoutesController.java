package de.zahra.kino.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Lets the Vue frontend handle its own routes when Spring Boot serves it (in the container).
 * <p>
 * The frontend uses history routing, so its pages have real paths like /screenings/7.
 * When the guest reloads such a page, the browser asks this server for that path.
 * There is no controller and no file for it, so without this class the answer would be 404.
 * Instead, we hand out index.html; the Vue router then reads the path and shows the right page.
 * <p>
 * During development the Vite dev server does the same automatically.
 * New frontend routes must be added here as well. API paths start with /api and are not affected.
 */
@Controller
public class FrontendRoutesController {

    @GetMapping({"/screenings/**", "/confirmation"})
    public String forwardToFrontend() {
        // "forward" serves index.html internally; the address in the browser stays the same.
        return "forward:/index.html";
    }
}