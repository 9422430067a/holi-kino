package de.zahra.kino.reservation;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Column(name = "privacy_accepted_at", nullable = false)
    private Instant privacyAcceptedAt;

    // Null means the guest did not subscribe to the newsletter.
    @Column(name = "newsletter_accepted_at")
    private Instant newsletterAcceptedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Booking() {
        // required by JPA
    }

    public Booking(String fullName, LocalDate birthDate, String email, String phone,
                   Instant termsAcceptedAt, Instant privacyAcceptedAt,
                   Instant newsletterAcceptedAt, Instant createdAt) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.termsAcceptedAt = termsAcceptedAt;
        this.privacyAcceptedAt = privacyAcceptedAt;
        this.newsletterAcceptedAt = newsletterAcceptedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public Instant getPrivacyAcceptedAt() {
        return privacyAcceptedAt;
    }

    public Instant getNewsletterAcceptedAt() {
        return newsletterAcceptedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}