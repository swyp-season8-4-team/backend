package org.swyp.dessertbee.role.entity;

public enum RoleType {
    ROLE_USER("ROLE_USER", "일반 사용자"),
    ROLE_OWNER("ROLE_OWNER", "사업자"),
    ROLE_ADMIN("ROLE_ADMIN", "관리자");

    private final String roleName;
    private final String description;

    RoleType(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromString(String role) {
        try {
            return valueOf(role);
        } catch (IllegalArgumentException e) {
            return ROLE_USER; // 기본값
        }
    }
}