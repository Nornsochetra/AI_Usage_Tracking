package com.admin.backend.user.service;

import com.admin.backend.user.dto.CreateUserRequest;
import com.admin.backend.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse createUser(CreateUserRequest request);
}
