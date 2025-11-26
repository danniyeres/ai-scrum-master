package org.example.aiscrummaster.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "team_members")

public class TeamMember {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String telegramId;
    private String role;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "team_id", nullable = false)
    private Team team;
}
