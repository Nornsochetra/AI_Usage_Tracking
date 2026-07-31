package com.admin.backend.common.domain;

import com.admin.backend.common.enumeration.AiProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "api_keys",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_provider",
                columnNames = {"user_id", "provider"}
        )
)
@Getter @Setter
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiProvider provider;

    @Column(nullable = false, unique = true, length = 64)
    private String keyValue;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
