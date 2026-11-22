# Holi Lichtspielhaus

Online-Sitzplatzreservierung für ein Kino mit einem Saal (10 Reihen × 20 Plätze): Vorstellung wählen, einen Platz für
fünf Minuten reservieren, Daten eingeben und verbindlich buchen.

## Schnellstart

Voraussetzung ist nur Docker.

```bash
docker compose up --build
```

Danach läuft die Anwendung unter http://localhost:8080. Liquibase legt das Schema an und füllt es mit
Beispielvorstellungen. Die Termine liegen bewusst im März 2036, damit sie immer in der Zukunft sind.

## Wie Doppelbuchungen verhindert werden

- **Reservieren in einem einzigen SQL-Statement.** Statt erst zu prüfen und dann zu schreiben, reserviert
  `INSERT … ON CONFLICT … DO UPDATE … WHERE abgelaufen` atomar. Der Unique Constraint auf
  `(screening_id, seat_row, seat_number)` lässt Postgres parallele Anfragen auf denselben Platz nacheinander abarbeiten.
  Eine betroffene Zeile heißt: Der Platz gehört dir. Null heißt: vergeben (409). Das funktioniert auch mit mehreren
  Instanzen der Anwendung.
- **Die Frist entscheidet, nicht der Scheduler.** Eine Reservierung ist aktiv, solange `reserved_until > now`. Diese
  eine Regel gilt beim Reservieren, im Sitzplan und beim Buchen. Der Scheduler löscht abgelaufene Zeilen nur, um die
  Tabelle klein zu halten.
- **Buchen nur mit gültigem Token.** Ein `UPDATE` setzt den Platz nur dann auf gebucht, wenn Token, Status und Frist
  passen. Sonst antwortet die API mit 409, und die angelegte Buchung wird zurückgerollt.
- **Die Datenbank als letzte Verteidigungslinie.** CHECK-Constraints erzwingen zusätzlich, dass ein gebuchter Platz eine
  Buchung und keine Frist hat.

## Technik

- **Backend:** Java 21, Spring Boot 4, PostgreSQL 17, Liquibase
- **Frontend:** Vue 3 mit TypeScript und Vite, Pinia, Vue Router, axios, VeeValidate
- **Tests:** JUnit mit Testcontainers und MockMvc, Vitest mit Vue Test Utils
- **Betrieb:** ein Container. Spring Boot liefert das gebaute Frontend selbst aus.

## API

| Methode | Pfad                                | Zweck                                                   |
|---------|-------------------------------------|---------------------------------------------------------|
| GET     | `/api/screenings`                   | kommende Vorstellungen                                  |
| GET     | `/api/screenings/{id}`              | eine Vorstellung                                        |
| GET     | `/api/screenings/{id}/seats`        | Sitzplan mit freien, reservierten und gebuchten Plätzen |
| POST    | `/api/screenings/{id}/reservations` | Platz reservieren oder wechseln                         |
| POST    | `/api/bookings`                     | Reservierung verbindlich buchen                         |

Fehler kommen einheitlich als `ProblemDetail`.

| Status | Titel                          | Wann                                                                                               |
|--------|--------------------------------|----------------------------------------------------------------------------------------------------|
| 400    | `Invalid request data`         | Formularfelder ungültig; `errors` enthält eine Meldung pro Feld                                    |
| 400    | `Bad Request`                  | Platz außerhalb des Saals oder JSON nicht lesbar, etwa ein unmögliches Datum                       |
| 404    | `Not Found`                    | Vorstellung existiert nicht; bei Sitzplan und Reservierung auch, wenn sie schon begonnen hat       |
| 409    | `Seat already taken`           | Platz ist von jemand anderem reserviert oder gebucht; eine bestehende Reservierung bleibt erhalten |
| 409    | `Reservation no longer active` | Reservierung abgelaufen, schon gebucht oder inzwischen geändert; Platz neu wählen                  |

Beide 409 haben einen eigenen Titel, weil das Frontend unterschiedlich reagiert: anderen Platz wählen oder ganz neu
beginnen.

## Entwicklung

```bash
docker compose up -d db                      # nur die Datenbank
cd backend && ./mvnw spring-boot:run         # API auf Port 8080
cd frontend && npm install && npm run dev    # Oberfläche auf http://localhost:5173
```

Vite leitet Anfragen an `/api` an das Backend weiter. Der Container und das lokal gestartete Backend benutzen beide Port
8080, also nicht gleichzeitig starten.

**Tests:**

```bash
cd backend && ./mvnw test                    # braucht Docker (Testcontainers)
cd frontend && npm run test:unit -- --run
```

Der Docker-Build überspringt die Backend-Tests, weil Testcontainers innerhalb eines Docker-Builds nicht läuft.

## Konfiguration

| Einstellung                         | Standard | Bedeutung                                |
|-------------------------------------|----------|------------------------------------------|
| `kino.reservation.duration`         | `PT5M`   | Dauer einer Reservierung                 |
| `kino.reservation.cleanup.enabled`  | `true`   | Scheduler für abgelaufene Reservierungen |
| `kino.reservation.cleanup.interval` | `PT1M`   | Abstand zwischen zwei Läufen             |

Alle Einstellungen lassen sich auch als Umgebungsvariable setzen, etwa `KINO_RESERVATION_DURATION=PT30S` zum
Ausprobieren.

## Annahmen und Grenzen

- **Ein Platz pro Buchung.** Wer einen anderen Platz wählt, gibt den alten sofort frei. Die ursprüngliche Frist bleibt,
  damit niemand durch Wechseln endlos blockiert.
- **Bewusst nicht umgesetzt**, laut Aufgabe nicht gefordert: Verwaltungsoberfläche, Zahlung, Benutzerkonten,
  E-Mail-Versand.
- **Token im Browser:** Das Reservierungstoken liegt in `sessionStorage`, damit ein Neuladen die Reservierung nicht
  verliert.
- **Polling:** Der Sitzplan aktualisiert sich alle fünf Sekunden. WebSockets oder Server-Sent Events wären die
  Alternative.
- **Uhr im Browser:** Der Countdown ist nur eine Anzeige. Ob die Frist abgelaufen ist, entscheidet immer der Server.

