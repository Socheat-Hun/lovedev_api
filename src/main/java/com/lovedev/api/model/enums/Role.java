package com.lovedev.api.model.enums;

public enum Role {
    USER,
    EMPLOYEE,
    MANAGER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}