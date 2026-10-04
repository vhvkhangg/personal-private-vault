package com.vhvkhangg.personalprivatevault.journal.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Diary entry entity mapped to {@code diary_entries}.
 */
@Entity
@Table(name = "diary_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "text")
    private String contentMarkdown;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public DiaryEntry(LocalDate entryDate, String title, String contentMarkdown) {
        this.entryDate = Objects.requireNonNull(entryDate, "entryDate must not be null");
        this.title = title;
        this.contentMarkdown = Objects.requireNonNull(contentMarkdown, "contentMarkdown must not be null");
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public void update(LocalDate entryDate, String title, String contentMarkdown) {
        this.entryDate = Objects.requireNonNull(entryDate, "entryDate must not be null");
        this.title = title;
        this.contentMarkdown = Objects.requireNonNull(contentMarkdown, "contentMarkdown must not be null");
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
            this.updatedAt = Instant.now();
        }
    }

    public void restore() {
        if (this.deletedAt != null) {
            this.deletedAt = null;
            this.updatedAt = Instant.now();
        }
    }
}
