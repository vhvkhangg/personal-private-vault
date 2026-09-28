package com.vhvkhangg.personalprivatevault.settings.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "app_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppSettings {

    public static final short SINGLETON_ID = 1;
    public static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";
    public static final int DEFAULT_PAGINATION_SIZE = 20;
    public static final int DEFAULT_AUTO_LOCK_MINUTES = 5;
    public static final boolean DEFAULT_BACKUP_ENABLED = false;

    @Id
    @Column(name = "id", nullable = false)
    private Short id = SINGLETON_ID;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "default_currency_code", nullable = false, length = 3)
    private String defaultCurrencyCode;

    @Column(name = "pagination_size", nullable = false)
    private int paginationSize;

    @Column(name = "private_mode_auto_lock_minutes", nullable = false)
    private int privateModeAutoLockMinutes;

    @Column(name = "backup_enabled", nullable = false)
    private boolean backupEnabled;

    @Column(name = "backup_interval_hours")
    private Integer backupIntervalHours;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AppSettings(String timezone,
                       String defaultCurrencyCode,
                       int paginationSize,
                       int privateModeAutoLockMinutes,
                       boolean backupEnabled,
                       Integer backupIntervalHours,
                       Instant updatedAt) {
        this.id = SINGLETON_ID;
        this.timezone = Objects.requireNonNull(timezone, "timezone must not be null");
        this.defaultCurrencyCode = Objects.requireNonNull(defaultCurrencyCode, "defaultCurrencyCode must not be null");
        this.paginationSize = paginationSize;
        this.privateModeAutoLockMinutes = privateModeAutoLockMinutes;
        this.backupEnabled = backupEnabled;
        this.backupIntervalHours = backupIntervalHours;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public void update(String timezone,
                       String defaultCurrencyCode,
                       int paginationSize,
                       int privateModeAutoLockMinutes,
                       boolean backupEnabled,
                       Integer backupIntervalHours,
                       Instant updatedAt) {
        this.timezone = Objects.requireNonNull(timezone, "timezone must not be null");
        this.defaultCurrencyCode = Objects.requireNonNull(defaultCurrencyCode, "defaultCurrencyCode must not be null");
        this.paginationSize = paginationSize;
        this.privateModeAutoLockMinutes = privateModeAutoLockMinutes;
        this.backupEnabled = backupEnabled;
        this.backupIntervalHours = backupIntervalHours;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
