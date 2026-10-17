package com.vhvkhangg.personalprivatevault.fiction.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.fiction.source.FictionSourceOperations;
import com.vhvkhangg.personalprivatevault.fiction.source.FictionSourceProjection;
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
public class FictionSourceService implements FictionSourceOperations {

    private final FictionStoryArchetypeRepository storyArchetypeRepository;
    private final FictionWorldSettingRepository worldSettingRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final EntityManager entityManager;

    public FictionSourceService(
            FictionStoryArchetypeRepository storyArchetypeRepository,
            FictionWorldSettingRepository worldSettingRepository,
            VaultEntryOperations vaultEntryOperations,
            EntityManager entityManager) {
        this.storyArchetypeRepository = Objects.requireNonNull(storyArchetypeRepository, "storyArchetypeRepository must not be null");
        this.worldSettingRepository = Objects.requireNonNull(worldSettingRepository, "worldSettingRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    public Optional<FictionSourceProjection> findSource(Long id) {
        return id==null?Optional.empty():findSources(List.of(id)).stream().findFirst();
    }

    @Override
    public List<FictionSourceProjection> findSources(Collection<Long> fictionIds) {
        if (fictionIds == null || fictionIds.isEmpty()) {
            return List.of();
        }
        List<Long> cleanIds = fictionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleanIds.isEmpty()) {
            return List.of();
        }
        if (cleanIds.size()>100) throw new IllegalArgumentException("Source selection budget exceeded");
        Number bytes=(Number)entityManager.createQuery("SELECT sum(coalesce(function('octet_length',f.description),0)) FROM Fiction f WHERE f.id IN :ids AND f.isNsfw = false")
                .setParameter("ids",cleanIds).getSingleResult();
        if(bytes!=null && bytes.longValue()>1048576)throw new IllegalStateException("Owner source byte budget exceeded");
        List<FictionSourceValues> fictions = entityManager.createQuery(
                "SELECT new com.vhvkhangg.personalprivatevault.fiction.internal.application.FictionSourceValues(f.id,f.title,f.originalTitle,f.format,f.nationalityCode,f.genreId,f.progressStatus,f.consumptionStatus,f.totalChapters,f.description,f.isNsfw) FROM Fiction f WHERE f.id IN :ids AND f.isNsfw = false",
                FictionSourceValues.class).setParameter("ids",cleanIds).getResultList();
        if (fictions.isEmpty()) {
            return List.of();
        }
        Map<Long, VaultEntryView> vaultMap = vaultEntryOperations.findAll(cleanIds).stream()
                .collect(Collectors.toMap(VaultEntryView::id, v -> v));

        Map<Long, Set<Long>> storyArchetypesMap = storyArchetypeRepository.findByIdFictionIdIn(cleanIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getId().getFictionId(),
                        Collectors.mapping(a -> a.getId().getStoryArchetypeId(), Collectors.toSet())
                ));

        Map<Long, Set<Long>> worldSettingsMap = worldSettingRepository.findByIdFictionIdIn(cleanIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getId().getFictionId(),
                        Collectors.mapping(a -> a.getId().getWorldSettingId(), Collectors.toSet())
                ));

        return fictions.stream()
                .map(fiction -> {
                    VaultEntryView vaultEntry = vaultMap.get(fiction.id());
                    if (vaultEntry == null) {
                        return null;
                    }
                    Set<Long> saIds = storyArchetypesMap.getOrDefault(fiction.id(), Set.of());
                    Set<Long> wsIds = worldSettingsMap.getOrDefault(fiction.id(), Set.of());
                    return toProjection(fiction, vaultEntry, saIds, wsIds);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<FictionSourceProjection> enumerateSources(int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(100, limit));
        List<Long> ids = entityManager.createQuery(
                        "SELECT f.id FROM Fiction f ORDER BY f.id ASC", Long.class)
                .setFirstResult(safeOffset)
                .setMaxResults(safeLimit)
                .getResultList();
        return findSources(ids);
    }

    private FictionSourceProjection toProjection(
            FictionSourceValues fiction,
            VaultEntryView vaultEntry,
            Set<Long> storyArchetypeIds,
            Set<Long> worldSettingIds) {

        String contentHash = sha256(computeContentRevisionHash(fiction, storyArchetypeIds, worldSettingIds)
                );
        String descriptionHash = computeDescriptionRevisionHash(fiction.description());

        return new FictionSourceProjection(
                fiction.id(),
                fiction.title(),
                fiction.originalTitle(),
                fiction.format(),
                fiction.nationalityCode(),
                fiction.genreId(),
                storyArchetypeIds,
                worldSettingIds,
                fiction.progressStatus(),
                fiction.consumptionStatus(),
                fiction.totalChapters(),
                fiction.description(),
                fiction.isNsfw(),
                vaultEntry.deletedAt(),
                vaultEntry.entryType(),
                contentHash,
                descriptionHash
        );
    }

    public static String computeContentRevisionHash(
            FictionSourceValues fiction,
            Set<Long> storyArchetypeIds,
            Set<Long> worldSettingIds) {

        StringBuilder sb = new StringBuilder();
        appendField(sb, "id", fiction.id());
        appendField(sb, "title", fiction.title());
        appendField(sb, "originalTitle", fiction.originalTitle());
        appendField(sb, "format", fiction.format());
        appendField(sb, "nationalityCode", fiction.nationalityCode());
        appendField(sb, "genreId", fiction.genreId());
        appendCollection(sb, "storyArchetypes", storyArchetypeIds);
        appendCollection(sb, "worldSettings", worldSettingIds);
        appendField(sb, "progressStatus", fiction.progressStatus());
        appendField(sb, "consumptionStatus", fiction.consumptionStatus());
        appendField(sb, "totalChapters", fiction.totalChapters());
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
