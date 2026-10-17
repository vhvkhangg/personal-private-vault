package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreAssignmentRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.film.source.FilmSourceOperations;
import com.vhvkhangg.personalprivatevault.film.source.FilmSourceProjection;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FilmSourceService implements FilmSourceOperations {

    private final FilmGenreAssignmentRepository filmGenreAssignmentRepository;
    private final FilmStoryArchetypeRepository storyArchetypeRepository;
    private final FilmWorldSettingRepository worldSettingRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final EntityManager entityManager;

    public FilmSourceService(
            FilmGenreAssignmentRepository filmGenreAssignmentRepository,
            FilmStoryArchetypeRepository storyArchetypeRepository,
            FilmWorldSettingRepository worldSettingRepository,
            VaultEntryOperations vaultEntryOperations,
            EntityManager entityManager) {
        this.filmGenreAssignmentRepository = Objects.requireNonNull(filmGenreAssignmentRepository, "filmGenreAssignmentRepository must not be null");
        this.storyArchetypeRepository = Objects.requireNonNull(storyArchetypeRepository, "storyArchetypeRepository must not be null");
        this.worldSettingRepository = Objects.requireNonNull(worldSettingRepository, "worldSettingRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    public Optional<FilmSourceProjection> findSource(Long id) {
        return id==null?Optional.empty():findSources(List.of(id)).stream().findFirst();
    }

    @Override
    public List<FilmSourceProjection> findSources(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return List.of();
        }
        List<Long> cleanIds = filmIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleanIds.isEmpty()) {
            return List.of();
        }
        if (cleanIds.size()>100) throw new IllegalArgumentException("Source selection budget exceeded");
        Number bytes=(Number)entityManager.createQuery("SELECT sum(coalesce(function('octet_length',f.description),0)) FROM Film f WHERE f.id IN :ids AND f.isNsfw = false")
                .setParameter("ids",cleanIds).getSingleResult();
        if(bytes!=null && bytes.longValue()>1048576)throw new IllegalStateException("Owner source byte budget exceeded");
        List<FilmSourceValues> films = entityManager.createQuery(
                "SELECT new com.vhvkhangg.personalprivatevault.film.internal.application.FilmSourceValues(f.id,f.title,f.originalTitle,f.format,f.nationalityCode,f.productionStyle,f.progressStatus,f.consumptionStatus,f.totalEpisodes,f.description,f.isNsfw) FROM Film f WHERE f.id IN :ids AND f.isNsfw = false",
                FilmSourceValues.class).setParameter("ids",cleanIds).getResultList();
        if (films.isEmpty()) {
            return List.of();
        }
        Map<Long, VaultEntryView> vaultMap = vaultEntryOperations.findAll(cleanIds).stream()
                .collect(Collectors.toMap(VaultEntryView::id, v -> v));

        Map<Long, Set<Long>> genresMap = filmGenreAssignmentRepository.findByIdFilmIdIn(cleanIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getId().getFilmId(),
                        Collectors.mapping(a -> a.getId().getGenreId(), Collectors.toSet())
                ));

        Map<Long, Set<Long>> storyArchetypesMap = storyArchetypeRepository.findByIdFilmIdIn(cleanIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getId().getFilmId(),
                        Collectors.mapping(a -> a.getId().getStoryArchetypeId(), Collectors.toSet())
                ));

        Map<Long, Set<Long>> worldSettingsMap = worldSettingRepository.findByIdFilmIdIn(cleanIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getId().getFilmId(),
                        Collectors.mapping(a -> a.getId().getWorldSettingId(), Collectors.toSet())
                ));

        return films.stream()
                .map(film -> {
                    VaultEntryView vaultEntry = vaultMap.get(film.id());
                    if (vaultEntry == null) {
                        return null;
                    }
                    Set<Long> gIds = genresMap.getOrDefault(film.id(), Set.of());
                    Set<Long> saIds = storyArchetypesMap.getOrDefault(film.id(), Set.of());
                    Set<Long> wsIds = worldSettingsMap.getOrDefault(film.id(), Set.of());
                    return toProjection(film, vaultEntry, gIds, saIds, wsIds);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<FilmSourceProjection> enumerateSources(int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(100, limit));
        List<Long> ids = entityManager.createQuery(
                        "SELECT f.id FROM Film f ORDER BY f.id ASC", Long.class)
                .setFirstResult(safeOffset)
                .setMaxResults(safeLimit)
                .getResultList();
        return findSources(ids);
    }

    private FilmSourceProjection toProjection(
            FilmSourceValues film,
            VaultEntryView vaultEntry,
            Set<Long> genreIds,
            Set<Long> storyArchetypeIds,
            Set<Long> worldSettingIds) {

        String contentHash = sha256(computeContentRevisionHash(film, genreIds, storyArchetypeIds, worldSettingIds)
                );
        String descriptionHash = computeDescriptionRevisionHash(film.description());

        return new FilmSourceProjection(
                film.id(),
                film.title(),
                film.originalTitle(),
                film.format(),
                film.productionStyle(),
                film.nationalityCode(),
                genreIds,
                storyArchetypeIds,
                worldSettingIds,
                film.progressStatus(),
                film.consumptionStatus(),
                film.totalEpisodes(),
                film.description(),
                film.isNsfw(),
                vaultEntry.deletedAt(),
                vaultEntry.entryType(),
                contentHash,
                descriptionHash
        );
    }

    public static String computeContentRevisionHash(
            FilmSourceValues film,
            Set<Long> genreIds,
            Set<Long> storyArchetypeIds,
            Set<Long> worldSettingIds) {

        StringBuilder sb = new StringBuilder();
        appendField(sb, "id", film.id());
        appendField(sb, "title", film.title());
        appendField(sb, "originalTitle", film.originalTitle());
        appendField(sb, "format", film.format());
        appendField(sb, "productionStyle", film.productionStyle());
        appendField(sb, "nationalityCode", film.nationalityCode());
        appendCollection(sb, "genres", genreIds);
        appendCollection(sb, "storyArchetypes", storyArchetypeIds);
        appendCollection(sb, "worldSettings", worldSettingIds);
        appendField(sb, "progressStatus", film.progressStatus());
        appendField(sb, "consumptionStatus", film.consumptionStatus());
        appendField(sb, "totalEpisodes", film.totalEpisodes());
        return sha256(sb.toString());
    }

    public static String computeDescriptionRevisionHash(String description) {
        StringBuilder value = new StringBuilder();
        appendField(value,"description",description);
        return sha256(value.toString());
    }

    private static void appendField(StringBuilder sb, String name, Object val) {
        if (val == null) {
            sb.append(name).append(":N\n");
            return;
        }
        String s = val.toString();
        sb.append(name).append(":").append(s.length()).append(":").append(s).append("\n");
    }

    private static void appendCollection(StringBuilder sb, String name, Set<Long> ids) {
        int count = ids != null ? ids.size() : 0;
        sb.append(name).append(":").append(count).append("\n");
        if (ids != null) {
            ids.stream().sorted().forEach(id -> {
                String s = String.valueOf(id);
                sb.append(s.length()).append(":").append(s).append("\n");
            });
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
