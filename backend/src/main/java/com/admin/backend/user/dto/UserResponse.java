package com.admin.backend.user.dto;

import com.admin.backend.common.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private Role role;
    private String teamId;
    private String teamName;
    private LocalDateTime createdAt;
}
