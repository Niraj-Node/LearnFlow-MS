package com.lms.userservice.dto;

import com.lms.userservice.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String photoUrl;
}
