package com.vhvkhangg.personalprivatevault.knowledge.study.internal.application;

import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountMutationGuard;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.infrastructure.persistence.StudyItemRepository;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StudyExternalAccountGuard implements ExternalAccountMutationGuard {

    private final StudyItemRepository studyItemRepository;
    private final ReferenceCatalog referenceCatalog;

    @Override
    public void validateMutation(Long accountId, ExternalAccountType newType, Long newPlatformId) {
        if (accountId == null) {
            return;
        }
        if (studyItemRepository.existsByYoutubeChannelAccountId(accountId)) {
            if (newType != ExternalAccountType.YOUTUBE_CHANNEL) {
                throw new ExternalAccountConflictException(
                        "Cannot change account type away from YOUTUBE_CHANNEL because it is referenced by a Study item"
                );
            }
            if (newPlatformId != null) {
                Optional<PlatformView> platform = referenceCatalog.platform(newPlatformId);
                if (platform.isEmpty() || platform.get().name() == null || !"youtube".equalsIgnoreCase(platform.get().name().trim())) {
                    throw new ExternalAccountConflictException(
                            "Cannot change platform away from YouTube because account is referenced by a Study item"
                    );
                }
            }
        }
    }
}
