package com.vhvkhangg.personalprivatevault.fiction.internal.domain;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.util.Objects;

/**
 * Fiction work entity sharing identity with {@code vault_entries.id}.
 */
@Entity
@Table(name = "fictions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fiction implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "original_title", length = 500)
    private String originalTitle;

    @Column(name = "nationality_code", length = 2)
    private String nationalityCode;

    @Column(name = "poster_url", length = 2048)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "format", nullable = false)
    private FictionFormat format;

    @Column(name = "is_nsfw", nullable = false)
    private boolean isNsfw;

    @Column(name = "genre_id", nullable = false)
    private Long genreId;

    @Column(name = "author_person_id")
    private Long authorPersonId;

    @Column(name = "author_group_id")
    private Long authorGroupId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "total_chapters")
    private Integer totalChapters;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "progress_status", nullable = false)
    private ProgressStatus progressStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "consumption_status", nullable = false)
    private ConsumptionStatus consumptionStatus;

    @Column(name = "current_progress_text", length = 255)
    private String currentProgressText;

    @Column(name = "review", columnDefinition = "text")
    private String review;

    @Transient
    private boolean isNew = false;

    public Fiction(
            Long id,
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FictionFormat format,
            boolean isNsfw,
            Long genreId,
            Long authorPersonId,
            Long authorGroupId,
            String description,
            Integer totalChapters,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.originalTitle = originalTitle;
        this.nationalityCode = nationalityCode;
        this.posterUrl = posterUrl;
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.isNsfw = isNsfw;
        this.genreId = Objects.requireNonNull(genreId, "genreId must not be null");
        this.authorPersonId = authorPersonId;
        this.authorGroupId = authorGroupId;
        this.description = description;
        this.totalChapters = totalChapters;
        this.progressStatus = Objects.requireNonNull(progressStatus, "progressStatus must not be null");
        this.consumptionStatus = Objects.requireNonNull(consumptionStatus, "consumptionStatus must not be null");
        this.currentProgressText = currentProgressText;
        this.review = review;
        this.isNew = true;
    }

    public void update(
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FictionFormat format,
            boolean isNsfw,
            Long genreId,
            Long authorPersonId,
            Long authorGroupId,
            String description,
            Integer totalChapters,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.originalTitle = originalTitle;
        this.nationalityCode = nationalityCode;
        this.posterUrl = posterUrl;
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.isNsfw = isNsfw;
        this.genreId = Objects.requireNonNull(genreId, "genreId must not be null");
        this.authorPersonId = authorPersonId;
        this.authorGroupId = authorGroupId;
        this.description = description;
        this.totalChapters = totalChapters;
        this.progressStatus = Objects.requireNonNull(progressStatus, "progressStatus must not be null");
        this.consumptionStatus = Objects.requireNonNull(consumptionStatus, "consumptionStatus must not be null");
        this.currentProgressText = currentProgressText;
        this.review = review;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}
