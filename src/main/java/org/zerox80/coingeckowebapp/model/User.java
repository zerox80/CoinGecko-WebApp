package org.zerox80.coingeckowebapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter // Lombok Setter hinzugefügt, um die Rolle setzen zu können, falls nötig
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // Neues Feld für die Rolle des Benutzers
    @Column(nullable = false)
    private String role = "ROLE_USER"; // Standardrolle für neue Benutzer

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Portfolio portfolio;

    // Standardkonstruktor für JPA
    public User() {
    }

    // Konstruktor für die Erstellung eines neuen Benutzers
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        // Die Rolle wird standardmäßig auf "ROLE_USER" gesetzt
    }

    // Getter und Setter werden von Lombok (@Getter, @Setter) generiert
}