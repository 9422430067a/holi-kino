package de.zahra.kino.screening;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "screening")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    protected Screening() {
        // required by JPA
    }

    public Long getId() { return id; }
    public Movie getMovie() { return movie; }
    public Instant getStartsAt() { return startsAt; }
}