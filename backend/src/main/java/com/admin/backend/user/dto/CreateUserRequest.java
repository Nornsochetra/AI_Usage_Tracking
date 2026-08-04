package com.admin.backend.user.dto;

import com.admin.backend.common.enumeration.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String email;
    private String fullName;
    private Role role;
    private String teamId;
}
