package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for feed source management and scheduling.
 */
@Service
@RequiredArgsConstructor
public class FeedSourceService implements FeedSourceOperations {

    private final FeedSourceRepository feedSourceRepository;

    @Override
    @Transactional
    public FeedSourceView createSource(CreateFeedSourceCommand command) {
        validateCommon(command.name(), command.type(), command.sourceUrl(), command.feedUrl());

        boolean enabled = command.enabled() != null ? command.enabled() : true;
        boolean scheduled = command.scheduledRefreshEnabled() != null ? command.scheduledRefreshEnabled() : false;
        Integer interval = command.refreshIntervalMinutes();

        validateSchedule(scheduled, interval);

        FeedSource source = new FeedSource(
                command.name().trim(),
                command.type(),
                trimUrl(command.sourceUrl()),
                trimUrl(command.feedUrl()),
                enabled,
                scheduled,
                interval,
                command.config()
        );

        FeedSource saved = feedSourceRepository.save(source);
        return toView(saved);
    }

    @Override
    @Transactional
    public FeedSourceView updateSource(Long id, UpdateFeedSourceCommand command) {
        FeedSource source = feedSourceRepository.findById(id)
                .orElseThrow(() -> new FeedSourceNotFoundException(id));

        validateCommon(command.name(), command.type(), command.sourceUrl(), command.feedUrl());

        boolean enabled = command.enabled() != null ? command.enabled() : true;
        boolean scheduled = command.scheduledRefreshEnabled() != null ? command.scheduledRefreshEnabled() : false;
        Integer interval = command.refreshIntervalMinutes();

        validateSchedule(scheduled, interval);

        source.update(
                command.name().trim(),
                command.type(),
                trimUrl(command.sourceUrl()),
                trimUrl(command.feedUrl()),
                enabled,
                scheduled,
                interval,
                command.config()
        );

        if (!scheduled) {
            source.setNextFetchAt(null);
        } else if (source.getLastFetchedAt() == null) {
            source.setNextFetchAt(null);
        } else {
            source.setNextFetchAt(source.getLastFetchedAt().plus(Duration.ofMinutes(interval)));
        }

        FeedSource saved = feedSourceRepository.save(source);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedSourceView> findSourceById(Long id) {
        return feedSourceRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedSourceView> findDueSources(Instant cutoff, int limit) {
        if (limit <= 0) {
            throw new InvalidFeedSourceException("Limit must be positive");
        }
        Instant safeCutoff = cutoff != null ? cutoff : Instant.now();
        return feedSourceRepository.findDueSources(safeCutoff, PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    private void validateCommon(String name, FeedSourceType type, String sourceUrl, String feedUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidFeedSourceException("Feed source name must not be blank");
        }
        if (name.trim().length() > 255) {
            throw new InvalidFeedSourceException("Feed source name must not exceed 255 characters");
        }
        if (type == null) {
            throw new InvalidFeedSourceException("Feed source type must not be null");
        }
        if (sourceUrl != null && sourceUrl.trim().length() > 2048) {
            throw new InvalidFeedSourceException("sourceUrl must not exceed 2048 characters");
        }
        if (feedUrl != null && feedUrl.trim().length() > 2048) {
            throw new InvalidFeedSourceException("feedUrl must not exceed 2048 characters");
        }
    }

    private void validateSchedule(boolean scheduled, Integer interval) {
        if (interval != null && interval <= 0) {
            throw new InvalidFeedSourceException("Refresh interval must be greater than zero");
        }
        if (scheduled && interval == null) {
            throw new InvalidFeedSourceException("Scheduled refresh requires a positive refresh interval");
        }
    }

    private String trimUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }

    private FeedSourceView toView(FeedSource s) {
        return new FeedSourceView(
                s.getId(),
                s.getName(),
                s.getType(),
                s.getSourceUrl(),
                s.getFeedUrl(),
                s.isEnabled(),
                s.isScheduledRefreshEnabled(),
                s.getRefreshIntervalMinutes(),
                s.getConfig(),
                s.getLastFetchedAt(),
                s.getNextFetchAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
