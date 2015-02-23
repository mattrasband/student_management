package com.mattrasband.mgmt.service;

/**
 * Service default configurations.
 */
public enum ServiceProperties {
    DB_HOST("localhost"),
    DB_DATABASE("students"),
    DB_USER(null),
    DB_PASS(null);

    private final Object value;

    private ServiceProperties(Object value) {
        this.value = value;
    }

    public static String getDbHost() {
        return (String) DB_HOST.value;
    }

    public static String getDbDatabase() {
        return (String) DB_DATABASE.value;
    }

    public static String getDbUser() {
        return (String) DB_USER.value;
    }

    public static String getDbPass() {
        return (String) DB_PASS.value;
    }
}
