package com.lexguard.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum UserRole {
    CLIENTE("CLIENTE", "Cliente de servicios legales"),
    ABOGADO("ABOGADO", "Abogado / Profesional legal"),
    ADMIN("ADMIN", "Administrador del sistema");

    private final String value;
    private final String description;

    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    
    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    
    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Rol no puede ser null");
        }
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException(
            String.format("Rol inválido: '%s'. Valores válidos: CLIENTE, ABOGADO, ADMIN", value)
        );
    }

    @Override
    public String toString() {
        return value;
    }
}
