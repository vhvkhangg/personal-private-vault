package com.vhvkhangg.personalprivatevault.collection.music.internal.application;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;
import com.vhvkhangg.personalprivatevault.collection.music.internal.domain.MusicTrack;
import com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence.MusicTrackPersonRepository;
import com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence.MusicTrackRepository;
import com.vhvkhangg.personalprivatevault.collection.music.music.CreateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.music.InvalidMusicException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicOperations;
import com.vhvkhangg.personalprivatevault.collection.music.music.UpdateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link MusicOperations}.
 */
@Service
@RequiredArgsConstructor
public class MusicService implements MusicOperations {

    private final MusicTrackRepository musicTrackRepository;
    private final MusicTrackPersonRepository musicTrackPersonRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final PersonOperations personOperations;
    private final ReferenceCatalog referenceCatalog;

    @Override
    @Transactional
    public MusicView create(CreateMusicCommand command) {
        if (command == null) {
            throw new InvalidMusicException("CreateMusicCommand must not be null");
        }

        ValidatedMusic validated = validateMusic(command.title(), command.version(), command.platformId(), command.url());

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.MUSIC);

        MusicTrack track = new MusicTrack(
                vaultEntry.id(),
                validated.title(),
                validated.version(),
                validated.platformId(),
                validated.url()
        );

        musicTrackRepository.saveAndFlush(track);
        return toView(track);
    }

    @Override
    @Transactional
    public MusicView update(Long id, UpdateMusicCommand command) {
        if (id == null) {
            throw new InvalidMusicException("Music ID must not be null");
        }
        if (command == null) {
            throw new InvalidMusicException("UpdateMusicCommand must not be null");
        }

        MusicTrack track = musicTrackRepository.findById(id)
                .orElseThrow(() -> new MusicNotFoundException(id));

        ValidatedMusic validated = validateMusic(command.title(), command.version(), command.platformId(), command.url());

        track.update(
                validated.title(),
                validated.version(),
                validated.platformId(),
                validated.url()
        );

        musicTrackRepository.saveAndFlush(track);
        return toView(track);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MusicView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return musicTrackRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public void addCredit(Long musicId, Long personId, MusicCreditRole role) {
        if (musicId == null) {
            throw new InvalidMusicException("Music ID must not be null");
        }
        if (!musicTrackRepository.existsById(musicId)) {
            throw new MusicNotFoundException(musicId);
        }
        if (personId == null) {
            throw new InvalidMusicException("Person ID must not be null");
        }
        if (role == null) {
            throw new InvalidMusicException("Credit role must not be null");
        }
        if (personOperations.find(personId).isEmpty()) {
            throw new InvalidMusicException("Person with id " + personId + " does not exist");
        }

        musicTrackPersonRepository.insertIfAbsent(musicId, personId, role.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicCreditView> findCredits(Long musicId, int limit) {
        if (musicId == null) {
            throw new InvalidMusicException("Music ID must not be null");
        }
        if (limit <= 0) {
            throw new InvalidMusicException("Limit must be positive: " + limit);
        }
        if (!musicTrackRepository.existsById(musicId)) {
            throw new MusicNotFoundException(musicId);
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        return musicTrackPersonRepository.findCreditsByMusicId(musicId, pageRequest).stream()
                .map(p -> new MusicCreditView(p.getId().getMusicId(), p.getId().getPersonId(), p.getId().getRole()))
                .toList();
    }

    private ValidatedMusic validateMusic(String rawTitle, MusicVersion rawVersion, Long platformId, String rawUrl) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new InvalidMusicException("Music title must not be blank");
        }
        String title = rawTitle.trim();
        if (title.length() > 500) {
            throw new InvalidMusicException("Music title must not exceed 500 characters");
        }

        MusicVersion version = rawVersion != null ? rawVersion : MusicVersion.ORIGINAL;

        if (platformId != null && referenceCatalog.platform(platformId).isEmpty()) {
            throw new InvalidMusicException("Platform with id " + platformId + " does not exist in reference catalog");
        }

        String url = trimOrNull(rawUrl);
        if (url != null && url.length() > 2048) {
            throw new InvalidMusicException("URL must not exceed 2048 characters");
        }

        return new ValidatedMusic(title, version, platformId, url);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private MusicView toView(MusicTrack track) {
        return new MusicView(
                track.getId(),
                track.getTitle(),
                track.getVersion(),
                track.getPlatformId(),
                track.getUrl()
        );
    }

    private record ValidatedMusic(
            String title,
            MusicVersion version,
            Long platformId,
            String url
    ) {}
}
