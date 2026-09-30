package com.vhvkhangg.personalprivatevault.knowledge.study.internal.domain;

import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Study item entity sharing identity with {@code vault_entries.id} for type {@code STUDY}.
 */
@Entity
@Table(name = "study_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyItem implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "poster_url", length = 2048)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private StudyType type;

    @Column(name = "site_domain", length = 255)
    private String siteDomain;

    @Column(name = "youtube_channel_account_id", unique = true)
    private Long youtubeChannelAccountId;

    @Column(name = "author_person_id")
    private Long authorPersonId;

    @Column(name = "author_group_id")
    private Long authorGroupId;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "price_amount", precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "review", columnDefinition = "text")
    private String review;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "learning_status", nullable = false)
    private StudyStatus learningStatus;

    @Column(name = "progress_percent", precision = 5, scale = 2)
    private BigDecimal progressPercent;

    @Column(name = "current_progress_text", length = 500)
    private String currentProgressText;

    @Transient
    private boolean isNew = false;

    public StudyItem(
            Long id,
            String title,
            String posterUrl,
            StudyType type,
            String siteDomain,
            Long youtubeChannelAccountId,
            Long authorPersonId,
            Long authorGroupId,
            LocalDate publishedDate,
            BigDecimal priceAmount,
            String currencyCode,
            String description,
            String url,
            String review,
            StudyStatus learningStatus,
            BigDecimal progressPercent,
            String currentProgressText,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "StudyItem id must not be null");
        this.title = Objects.requireNonNull(title, "StudyItem title must not be null");
        this.type = Objects.requireNonNull(type, "StudyItem type must not be null");
        this.posterUrl = posterUrl;
        this.siteDomain = siteDomain;
        this.youtubeChannelAccountId = youtubeChannelAccountId;
        this.authorPersonId = authorPersonId;
        this.authorGroupId = authorGroupId;
        this.publishedDate = publishedDate;
        this.priceAmount = priceAmount;
        this.currencyCode = currencyCode;
        this.description = description;
        this.url = url;
        this.review = review;
        this.learningStatus = Objects.requireNonNull(learningStatus, "StudyItem learningStatus must not be null");
        this.progressPercent = progressPercent;
        this.currentProgressText = currentProgressText;
        this.isNew = isNew;
    }

    public void update(
            String title,
            String posterUrl,
            StudyType type,
            String siteDomain,
            Long youtubeChannelAccountId,
            Long authorPersonId,
            Long authorGroupId,
            LocalDate publishedDate,
            BigDecimal priceAmount,
            String currencyCode,
            String description,
            String url,
            String review,
            StudyStatus learningStatus,
            BigDecimal progressPercent,
            String currentProgressText
    ) {
        this.title = Objects.requireNonNull(title, "StudyItem title must not be null");
        this.type = Objects.requireNonNull(type, "StudyItem type must not be null");
        this.posterUrl = posterUrl;
        this.siteDomain = siteDomain;
        this.youtubeChannelAccountId = youtubeChannelAccountId;
        this.authorPersonId = authorPersonId;
        this.authorGroupId = authorGroupId;
        this.publishedDate = publishedDate;
        this.priceAmount = priceAmount;
        this.currencyCode = currencyCode;
        this.description = description;
        this.url = url;
        this.review = review;
        this.learningStatus = Objects.requireNonNull(learningStatus, "StudyItem learningStatus must not be null");
        this.progressPercent = progressPercent;
        this.currentProgressText = currentProgressText;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
