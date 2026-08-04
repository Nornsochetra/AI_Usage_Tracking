package com.admin.backend.user.service.impl;

import com.admin.backend.common.domain.Team;
import com.admin.backend.common.domain.User;
import com.admin.backend.common.enumeration.Role;
import com.admin.backend.common.repository.TeamRepository;
import com.admin.backend.common.repository.UserRepository;
import com.admin.backend.user.dto.CreateUserRequest;
import com.admin.backend.user.dto.UserResponse;
import com.admin.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("User already exists: " + request.getEmail());
        });

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown team: " + request.getTeamId()));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : Role.MEMBER);
        user.setTeam(team);

        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .teamId(user.getTeam() != null ? user.getTeam().getId() : null)
                .teamName(user.getTeam() != null ? user.getTeam().getTeamName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
