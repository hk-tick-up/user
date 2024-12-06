package com.example.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_user_friend",
                columnNames = {"user", "friend"}
        )
})
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
    public static enum Status {
        FRIEND, REQUEST, NOTYET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend")
    private User friend;

    @Enumerated(EnumType.STRING)
    private Status status;
}
