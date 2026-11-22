package de.zahra.kino.screening;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query("""
            SELECT s FROM Screening s
            JOIN FETCH s.movie
            WHERE s.startsAt > :now
            ORDER BY s.startsAt
            """)
    List<Screening> findUpcomingWithMovie(@Param("now") Instant now);

    boolean existsByIdAndStartsAtAfter(Long id, Instant startsAt);

    @Query("""
            SELECT s FROM Screening s
            JOIN FETCH s.movie
            WHERE s.id = :id
            """)
    Optional<Screening> findByIdWithMovie(@Param("id") Long id);
}