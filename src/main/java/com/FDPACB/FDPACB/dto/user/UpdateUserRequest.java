package com.FDPACB.FDPACB.dto.user;

import com.FDPACB.FDPACB.enums.Role;

public record UpdateUserRequest(
        Role role,
        Boolean active
) {}
