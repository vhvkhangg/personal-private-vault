package com.vhvkhangg.personalprivatevault.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiRouteInventoryIntegrationTest extends AbstractWebIntegrationTest {

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/v1/auth/bootstrap/status",
            "/api/v1/auth/bootstrap",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/revoke"
    );

    record ExpectedRouteContract(
            String method,
            String path,
            String operationId,
            int successStatus,
            String requestDto,
            String responseDto,
            boolean expect404
    ) {}

    private static final String ACCEPTED_MANIFEST = """
            DELETE | /api/v1/finance/recurring-rules/{id} | softDeleteRecurringRule | 200 | null | ApiResponseRecurringTransactionRuleResponse | true
            DELETE | /api/v1/finance/subscriptions/{id} | softDeleteSubscription | 200 | null | ApiResponseSubscriptionResponse | true
            DELETE | /api/v1/finance/transactions/{id} | softDeleteFinancialTransaction | 200 | null | ApiResponseFinancialTransactionResponse | true
            DELETE | /api/v1/finance/wallets/{id} | softDeleteWallet | 200 | null | ApiResponseWalletResponse | true
            DELETE | /api/v1/journal/diary-entries/{id} | softDeleteDiaryEntry | 200 | null | ApiResponseDiaryEntryResponse | true
            DELETE | /api/v1/personal/profiles/{id} | softDeletePersonalProfile | 200 | null | ApiResponsePersonalProfileResponse | true
            DELETE | /api/v1/vault/entries/{id} | trashVaultEntry | 200 | null | ApiResponseVaultEntryResponse | true
            DELETE | /api/v1/vault/entries/{id}/favorite | unfavoriteVaultEntry | 200 | null | ApiResponseVoid | true
            DELETE | /api/v1/vault/entries/{id}/rating | removeVaultEntryRating | 200 | null | ApiResponseVoid | true
            DELETE | /api/v1/vault/entries/{id}/tags/{tagId} | detachVaultEntryTag | 200 | null | ApiResponseVoid | true
            GET | /api/v1/accounts | findAccountsByPlatform | 200 | null | ApiResponseListExternalAccountResponse | true
            GET | /api/v1/accounts/{id} | getExternalAccount | 200 | null | ApiResponseExternalAccountResponse | true
            GET | /api/v1/accounts/{ownerAccountId}/relationships | findRelationshipsByOwner | 200 | null | ApiResponseListExternalAccountRelationshipResponse | true
            GET | /api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId} | getAccountRelationship | 200 | null | ApiResponseExternalAccountRelationshipResponse | true
            GET | /api/v1/accounts/{ownerAccountId}/snapshots | findSnapshotsByOwner | 200 | null | ApiResponseListFollowerSnapshotResponse | true
            GET | /api/v1/accounts/snapshots/{id} | getFollowerSnapshot | 200 | null | ApiResponseFollowerSnapshotResponse | true
            GET | /api/v1/accounts/snapshots/{snapshotId}/entries | findSnapshotEntries | 200 | null | ApiResponseListFollowerSnapshotEntryResponse | true
            GET | /api/v1/addresses/{id} | getAddress | 200 | null | ApiResponseAddressResponse | true
            GET | /api/v1/albums/{id} | getAlbum | 200 | null | ApiResponseAlbumResponse | true
            GET | /api/v1/albums/{id}/image-count | getAlbumImageCount | 200 | null | ApiResponseLong | true
            GET | /api/v1/albums/{id}/images | listAlbumImages | 200 | null | ApiResponseListImageResponse | true
            GET | /api/v1/auth/bootstrap/status | checkBootstrapStatus | 200 | null | ApiResponseBootstrapStatusResponse | false
            GET | /api/v1/brands/{id} | getBrand | 200 | null | ApiResponseBrandResponse | true
            GET | /api/v1/collection/music/{id} | getCollectionMusic | 200 | null | ApiResponseMusicResponse | true
            GET | /api/v1/collection/music/{id}/credits | getCollectionMusicCredits | 200 | null | ApiResponseListMusicCreditResponse | true
            GET | /api/v1/collection/shopping/{id} | getCollectionShoppingItem | 200 | null | ApiResponseShoppingItemResponse | true
            GET | /api/v1/collection/software/{id} | getCollectionSoftwareItem | 200 | null | ApiResponseSoftwareItemResponse | true
            GET | /api/v1/collection/software/{id}/platforms | getCollectionSoftwarePlatforms | 200 | null | ApiResponseListSoftwarePlatformResponse | true
            GET | /api/v1/feed/items/{id} | getFeedItem | 200 | null | ApiResponseFeedItemResponse | true
            GET | /api/v1/feed/sources/{id} | getFeedSource | 200 | null | ApiResponseFeedSourceResponse | true
            GET | /api/v1/feed/sources/{sourceId}/items | findRecentFeedItemsBySource | 200 | null | ApiResponseListFeedItemResponse | true
            GET | /api/v1/feed/sources/due | findDueFeedSources | 200 | null | ApiResponseListFeedSourceResponse | true
            GET | /api/v1/fiction-genres/{id} | getFictionGenre | 200 | null | ApiResponseFictionGenreResponse | true
            GET | /api/v1/fiction-genres/by-name | getFictionGenreByName | 200 | null | ApiResponseFictionGenreResponse | true
            GET | /api/v1/fictions/{id} | getFiction | 200 | null | ApiResponseFictionResponse | true
            GET | /api/v1/fictions/{id}/classifications | getFictionClassifications | 200 | null | ApiResponseFictionClassificationsResponse | true
            GET | /api/v1/fictions/{id}/links | getFictionLinks | 200 | null | ApiResponseListFictionLinkResponse | true
            GET | /api/v1/fictions/{id}/links/{linkId} | getFictionLink | 200 | null | ApiResponseFictionLinkResponse | true
            GET | /api/v1/fictions/{id}/story-archetypes | getFictionStoryArchetypes | 200 | null | ApiResponseSetLong | true
            GET | /api/v1/fictions/{id}/world-settings | getFictionWorldSettings | 200 | null | ApiResponseSetLong | true
            GET | /api/v1/film-genres/{id} | getFilmGenre | 200 | null | ApiResponseFilmGenreResponse | true
            GET | /api/v1/film-genres/by-name | getFilmGenreByName | 200 | null | ApiResponseFilmGenreResponse | true
            GET | /api/v1/films/{id} | getFilm | 200 | null | ApiResponseFilmResponse | true
            GET | /api/v1/films/{id}/classifications | getFilmClassifications | 200 | null | ApiResponseFilmClassificationsResponse | true
            GET | /api/v1/films/{id}/credits | getFilmCredits | 200 | null | ApiResponseListFilmCreditResponse | true
            GET | /api/v1/films/{id}/genres | getFilmGenres | 200 | null | ApiResponseSetLong | true
            GET | /api/v1/films/{id}/links | getFilmLinks | 200 | null | ApiResponseListFilmLinkResponse | true
            GET | /api/v1/films/{id}/links/{linkId} | getFilmLink | 200 | null | ApiResponseFilmLinkResponse | true
            GET | /api/v1/films/{id}/story-archetypes | getFilmStoryArchetypes | 200 | null | ApiResponseSetLong | true
            GET | /api/v1/films/{id}/world-settings | getFilmWorldSettings | 200 | null | ApiResponseSetLong | true
            GET | /api/v1/films/credits/{creditId} | getFilmCredit | 200 | null | ApiResponseFilmCreditResponse | true
            GET | /api/v1/finance/categories | findTransactionCategories | 200 | null | ApiResponseListTransactionCategoryResponse | true
            GET | /api/v1/finance/categories/{id} | getTransactionCategory | 200 | null | ApiResponseTransactionCategoryResponse | true
            GET | /api/v1/finance/recurring-rules | findRecurringRules | 200 | null | ApiResponseListRecurringTransactionRuleResponse | true
            GET | /api/v1/finance/recurring-rules/{id} | getRecurringRule | 200 | null | ApiResponseRecurringTransactionRuleResponse | true
            GET | /api/v1/finance/recurring-rules/due | findDueRecurringRules | 200 | null | ApiResponseListRecurringTransactionRuleResponse | true
            GET | /api/v1/finance/subscriptions | findActiveSubscriptions | 200 | null | ApiResponseListSubscriptionResponse | true
            GET | /api/v1/finance/subscriptions/{id} | getSubscription | 200 | null | ApiResponseSubscriptionResponse | true
            GET | /api/v1/finance/transactions | findFinancialTransactions | 200 | null | ApiResponseListFinancialTransactionResponse | true
            GET | /api/v1/finance/transactions/{id} | getFinancialTransaction | 200 | null | ApiResponseFinancialTransactionResponse | true
            GET | /api/v1/finance/wallets | findWallets | 200 | null | ApiResponseListWalletResponse | true
            GET | /api/v1/finance/wallets/{id} | getWallet | 200 | null | ApiResponseWalletResponse | true
            GET | /api/v1/finance/wallets/{id}/balance | getWalletBalance | 200 | null | ApiResponseWalletBalanceResponse | true
            GET | /api/v1/images/{id} | getImage | 200 | null | ApiResponseImageResponse | true
            GET | /api/v1/images/{id}/content | downloadImageContent | 200 | null | binary | true
            GET | /api/v1/imports/jobs | findRecentImportJobs | 200 | null | ApiResponseListImportJobResponse | true
            GET | /api/v1/imports/jobs/{id} | getImportJob | 200 | null | ApiResponseImportJobResponse | true
            GET | /api/v1/imports/jobs/{id}/items | findImportJobItems | 200 | null | ApiResponseListImportJobItemResponse | true
            GET | /api/v1/journal/diary-entries | findDiaryEntries | 200 | null | ApiResponseListDiaryEntryResponse | true
            GET | /api/v1/journal/diary-entries/{id} | getDiaryEntry | 200 | null | ApiResponseDiaryEntryResponse | true
            GET | /api/v1/knowledge/information/{id} | getInformationItem | 200 | null | ApiResponseKnowledgeInformationResponse | true
            GET | /api/v1/knowledge/notes/{id} | getNote | 200 | null | ApiResponseKnowledgeNoteResponse | true
            GET | /api/v1/knowledge/study/{id} | getStudyItem | 200 | null | ApiResponseKnowledgeStudyResponse | true
            GET | /api/v1/knowledge/vocabulary/{id} | getVocabularyItem | 200 | null | ApiResponseKnowledgeVocabularyResponse | true
            GET | /api/v1/knowledge/vocabulary/{id}/reviews | getVocabularyReviews | 200 | null | ApiResponseListKnowledgeVocabularyReviewResponse | true
            GET | /api/v1/knowledge/vocabulary/due | getDueVocabularyItems | 200 | null | ApiResponseListKnowledgeVocabularyResponse | true
            GET | /api/v1/location-categories/{id} | getLocationCategory | 200 | null | ApiResponseLocationCategoryResponse | true
            GET | /api/v1/location-categories/by-name | getLocationCategoryByName | 200 | null | ApiResponseLocationCategoryResponse | true
            GET | /api/v1/locations/{id} | getLocation | 200 | null | ApiResponseLocationResponse | true
            GET | /api/v1/locations/{id}/business-hours | getLocationBusinessHours | 200 | null | ApiResponseBusinessHoursScheduleResponse | true
            GET | /api/v1/locations/{id}/categories | getLocationCategories | 200 | null | ApiResponseListLocationCategoryResponse | true
            GET | /api/v1/locations/{id}/dining-service-styles | getLocationDiningServiceStyles | 200 | null | ApiResponseSetDiningServiceStyle | true
            GET | /api/v1/people/{id} | getPerson | 200 | null | ApiResponsePersonResponse | true
            GET | /api/v1/people/{id}/roles | getPersonRoles | 200 | null | ApiResponseSetPersonRole | true
            GET | /api/v1/people/creator-groups/{id} | getCreatorGroup | 200 | null | ApiResponseCreatorGroupResponse | true
            GET | /api/v1/people/creator-groups/{id}/members | getCreatorGroupMembers | 200 | null | ApiResponseListCreatorGroupMemberResponse | true
            GET | /api/v1/personal/profiles | findPersonalProfiles | 200 | null | ApiResponseListPersonalProfileResponse | true
            GET | /api/v1/personal/profiles/{id} | getPersonalProfile | 200 | null | ApiResponsePersonalProfileResponse | true
            GET | /api/v1/personal/profiles/self | getSelfPersonalProfile | 200 | null | ApiResponsePersonalProfileResponse | true
            GET | /api/v1/reference/countries | listCountries | 200 | null | ApiResponseListCountryResponse | true
            GET | /api/v1/reference/countries/{code} | getCountry | 200 | null | ApiResponseCountryResponse | true
            GET | /api/v1/reference/currencies | listCurrencies | 200 | null | ApiResponseListCurrencyResponse | true
            GET | /api/v1/reference/currencies/{code} | getCurrency | 200 | null | ApiResponseCurrencyResponse | true
            GET | /api/v1/reference/languages | listLanguages | 200 | null | ApiResponseListLanguageResponse | true
            GET | /api/v1/reference/languages/{code} | getLanguage | 200 | null | ApiResponseLanguageResponse | true
            GET | /api/v1/reference/platforms | listPlatforms | 200 | null | ApiResponseListPlatformResponse | true
            GET | /api/v1/reference/platforms/{id} | getPlatform | 200 | null | ApiResponsePlatformResponse | true
            GET | /api/v1/reference/story-archetypes | listStoryArchetypes | 200 | null | ApiResponseListStoryArchetypeResponse | true
            GET | /api/v1/reference/story-archetypes/{id} | getStoryArchetype | 200 | null | ApiResponseStoryArchetypeResponse | true
            GET | /api/v1/reference/world-settings | listWorldSettings | 200 | null | ApiResponseListWorldSettingResponse | true
            GET | /api/v1/reference/world-settings/{id} | getWorldSetting | 200 | null | ApiResponseWorldSettingResponse | true
            GET | /api/v1/saved-resources | findSavedResources | 200 | null | ApiResponseListSavedResourceResponse | true
            GET | /api/v1/saved-resources/{id} | getSavedResource | 200 | null | ApiResponseSavedResourceResponse | true
            GET | /api/v1/saved-resources/{id}/conversions | getSavedResourceConversions | 200 | null | ApiResponseListSavedResourceConversionResponse | true
            GET | /api/v1/search | globalSearch | 200 | null | ApiResponseListGlobalSearchResultResponse | true
            GET | /api/v1/settings | getSettings | 200 | null | ApiResponseAppSettingsResponse | true
            GET | /api/v1/vault/entries/{id} | getVaultEntry | 200 | null | ApiResponseVaultEntryResponse | true
            GET | /api/v1/vault/entries/{id}/metadata | getVaultEntryMetadata | 200 | null | ApiResponseVaultMetadataResponse | true
            POST | /api/v1/accounts | createExternalAccount | 201 | CreateExternalAccountRequest | ApiResponseExternalAccountResponse | false
            POST | /api/v1/accounts/{ownerAccountId}/snapshots | createFollowerSnapshot | 201 | CreateFollowerSnapshotRequest | ApiResponseFollowerSnapshotResponse | true
            POST | /api/v1/addresses | createAddress | 201 | CreateAddressRequest | ApiResponseAddressResponse | false
            POST | /api/v1/albums | createAlbum | 201 | CreateAlbumRequest | ApiResponseAlbumResponse | false
            POST | /api/v1/auth/bootstrap | bootstrapVault | 201 | BootstrapRequest | ApiResponseAppUserResponse | false
            POST | /api/v1/auth/login | login | 200 | LoginRequest | ApiResponseAuthTokensResponse | false
            POST | /api/v1/auth/private-pin/verify | verifyPrivatePin | 200 | VerifyPinRequest | ApiResponseVerifyPinResponse | false
            POST | /api/v1/auth/refresh | refreshToken | 200 | RefreshTokenRequest | ApiResponseAuthTokensResponse | false
            POST | /api/v1/auth/revoke | revokeToken | 200 | RevokeTokenRequest | ApiResponseVoid | false
            POST | /api/v1/brands | createBrand | 201 | CreateBrandRequest | ApiResponseBrandResponse | false
            POST | /api/v1/collection/music | createCollectionMusic | 201 | CreateMusicRequest | ApiResponseMusicResponse | false
            POST | /api/v1/collection/shopping | createCollectionShoppingItem | 201 | CreateShoppingItemRequest | ApiResponseShoppingItemResponse | false
            POST | /api/v1/collection/software | createCollectionSoftwareItem | 201 | CreateSoftwareItemRequest | ApiResponseSoftwareItemResponse | false
            POST | /api/v1/feed/sources | createFeedSource | 201 | CreateFeedSourceRequest | ApiResponseFeedSourceResponse | false
            POST | /api/v1/fiction-genres | createFictionGenre | 201 | CreateFictionGenreRequest | ApiResponseFictionGenreResponse | false
            POST | /api/v1/fictions | createFiction | 201 | CreateFictionRequest | ApiResponseFictionResponse | false
            POST | /api/v1/fictions/{id}/links | createFictionLink | 201 | CreateFictionLinkRequest | ApiResponseFictionLinkResponse | true
            POST | /api/v1/film-genres | createFilmGenre | 201 | CreateFilmGenreRequest | ApiResponseFilmGenreResponse | false
            POST | /api/v1/films | createFilm | 201 | CreateFilmRequest | ApiResponseFilmResponse | false
            POST | /api/v1/films/{id}/credits | createFilmCredit | 201 | CreateFilmCreditRequest | ApiResponseFilmCreditResponse | true
            POST | /api/v1/films/{id}/links | createFilmLink | 201 | CreateFilmLinkRequest | ApiResponseFilmLinkResponse | true
            POST | /api/v1/finance/categories | createTransactionCategory | 201 | CreateTransactionCategoryRequest | ApiResponseTransactionCategoryResponse | false
            POST | /api/v1/finance/recurring-rules | createRecurringRule | 201 | CreateRecurringTransactionRuleRequest | ApiResponseRecurringTransactionRuleResponse | false
            POST | /api/v1/finance/recurring-rules/{id}/restore | restoreRecurringRule | 200 | null | ApiResponseRecurringTransactionRuleResponse | true
            POST | /api/v1/finance/subscriptions | createSubscription | 201 | CreateSubscriptionRequest | ApiResponseSubscriptionResponse | false
            POST | /api/v1/finance/subscriptions/{id}/restore | restoreSubscription | 200 | null | ApiResponseSubscriptionResponse | true
            POST | /api/v1/finance/transactions | createFinancialTransaction | 201 | CreateFinancialTransactionRequest | ApiResponseFinancialTransactionResponse | false
            POST | /api/v1/finance/transactions/{id}/restore | restoreFinancialTransaction | 200 | null | ApiResponseFinancialTransactionResponse | true
            POST | /api/v1/finance/wallets | createWallet | 201 | CreateWalletRequest | ApiResponseWalletResponse | false
            POST | /api/v1/finance/wallets/{id}/restore | restoreWallet | 200 | null | ApiResponseWalletResponse | true
            POST | /api/v1/images | createImage | 201 | CreateImageRequest | ApiResponseImageResponse | false
            POST | /api/v1/images/upload | uploadImage | 201 | multipart | ApiResponseImageResponse | false
            POST | /api/v1/imports/jobs | createImportJob | 201 | CreateImportJobRequest | ApiResponseImportJobResponse | false
            POST | /api/v1/imports/jobs/{id}/cancel | cancelImportJob | 200 | null | ApiResponseImportJobResponse | true
            POST | /api/v1/imports/jobs/{id}/execute | executeImportJob | 200 | ExecuteImportJobRequest | ApiResponseImportJobResponse | true
            POST | /api/v1/imports/jobs/{id}/parse | parseImportJob | 200 | ParseImportJobRequest | ApiResponseImportJobResponse | true
            POST | /api/v1/imports/jobs/{id}/validate | validateImportJob | 200 | null | ApiResponseImportJobResponse | true
            POST | /api/v1/journal/diary-entries | createDiaryEntry | 201 | CreateDiaryEntryRequest | ApiResponseDiaryEntryResponse | false
            POST | /api/v1/journal/diary-entries/{id}/restore | restoreDiaryEntry | 200 | null | ApiResponseDiaryEntryResponse | true
            POST | /api/v1/knowledge/information | createInformationItem | 201 | CreateKnowledgeInformationRequest | ApiResponseKnowledgeInformationResponse | false
            POST | /api/v1/knowledge/notes | createNote | 201 | CreateKnowledgeNoteRequest | ApiResponseKnowledgeNoteResponse | false
            POST | /api/v1/knowledge/study | createStudyItem | 201 | CreateKnowledgeStudyRequest | ApiResponseKnowledgeStudyResponse | false
            POST | /api/v1/knowledge/vocabulary | createVocabularyItem | 201 | CreateKnowledgeVocabularyRequest | ApiResponseKnowledgeVocabularyResponse | false
            POST | /api/v1/knowledge/vocabulary/{id}/reviews | reviewVocabularyItem | 200 | VocabularyReviewRequest | ApiResponseVocabularyReviewResultResponse | true
            POST | /api/v1/location-categories | createLocationCategory | 201 | CreateLocationCategoryRequest | ApiResponseLocationCategoryResponse | false
            POST | /api/v1/locations | createLocation | 201 | CreateLocationRequest | ApiResponseLocationResponse | false
            POST | /api/v1/people | createPerson | 201 | CreatePersonRequest | ApiResponsePersonResponse | false
            POST | /api/v1/people/creator-groups | createCreatorGroup | 201 | CreateCreatorGroupRequest | ApiResponseCreatorGroupResponse | false
            POST | /api/v1/personal/profiles | createPersonalProfile | 201 | CreatePersonalProfileRequest | ApiResponsePersonalProfileResponse | false
            POST | /api/v1/personal/profiles/{id}/restore | restorePersonalProfile | 200 | null | ApiResponsePersonalProfileResponse | true
            POST | /api/v1/portability/exports | exportVaultArchive | 200 | null | binary | false
            POST | /api/v1/saved-resources/{id}/convert/information | convertSavedResourceToInformation | 201 | ConvertToInformationRequest | ApiResponseSavedResourceConversionResponse | true
            POST | /api/v1/saved-resources/{id}/convert/note | convertSavedResourceToNote | 201 | ConvertToNoteRequest | ApiResponseSavedResourceConversionResponse | true
            POST | /api/v1/saved-resources/{id}/convert/study | convertSavedResourceToStudy | 201 | ConvertToStudyRequest | ApiResponseSavedResourceConversionResponse | true
            POST | /api/v1/saved-resources/feed-item | saveFeedItemResource | 201 | CreateFeedSavedResourceRequest | ApiResponseSavedResourceResponse | false
            POST | /api/v1/saved-resources/manual | saveManualResource | 201 | CreateManualSavedResourceRequest | ApiResponseSavedResourceResponse | false
            POST | /api/v1/vault/entries/{id}/restore | restoreVaultEntry | 200 | null | ApiResponseVaultEntryResponse | true
            POST | /api/v1/vault/tags | createTag | 201 | CreateTagRequest | ApiResponseTagResponse | false
            PUT | /api/v1/accounts/{id} | updateExternalAccount | 200 | UpdateExternalAccountRequest | ApiResponseExternalAccountResponse | true
            PUT | /api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId} | setAccountRelationship | 200 | SetExternalAccountRelationshipRequest | ApiResponseExternalAccountRelationshipResponse | true
            PUT | /api/v1/addresses/{id} | updateAddress | 200 | UpdateAddressRequest | ApiResponseAddressResponse | true
            PUT | /api/v1/albums/{id} | updateAlbum | 200 | UpdateAlbumRequest | ApiResponseAlbumResponse | true
            PUT | /api/v1/auth/private-pin | changePrivatePin | 200 | ChangePinRequest | ApiResponseVoid | false
            PUT | /api/v1/brands/{id} | updateBrand | 200 | UpdateBrandRequest | ApiResponseBrandResponse | true
            PUT | /api/v1/collection/music/{id} | updateCollectionMusic | 200 | UpdateMusicRequest | ApiResponseMusicResponse | true
            PUT | /api/v1/collection/music/{id}/credits | addCollectionMusicCredit | 200 | AddMusicCreditRequest | ApiResponseVoid | true
            PUT | /api/v1/collection/shopping/{id} | updateCollectionShoppingItem | 200 | UpdateShoppingItemRequest | ApiResponseShoppingItemResponse | true
            PUT | /api/v1/collection/software/{id} | updateCollectionSoftwareItem | 200 | UpdateSoftwareItemRequest | ApiResponseSoftwareItemResponse | true
            PUT | /api/v1/collection/software/{id}/platforms/{platformId} | addCollectionSoftwarePlatform | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/feed/sources/{id} | updateFeedSource | 200 | UpdateFeedSourceRequest | ApiResponseFeedSourceResponse | true
            PUT | /api/v1/fiction-genres/{id} | updateFictionGenre | 200 | UpdateFictionGenreRequest | ApiResponseFictionGenreResponse | true
            PUT | /api/v1/fictions/{id} | updateFiction | 200 | UpdateFictionRequest | ApiResponseFictionResponse | true
            PUT | /api/v1/fictions/{id}/links/{linkId} | updateFictionLink | 200 | UpdateFictionLinkRequest | ApiResponseFictionLinkResponse | true
            PUT | /api/v1/fictions/{id}/story-archetypes/{storyArchetypeId} | addFictionStoryArchetype | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/fictions/{id}/world-settings/{worldSettingId} | addFictionWorldSetting | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/film-genres/{id} | updateFilmGenre | 200 | UpdateFilmGenreRequest | ApiResponseFilmGenreResponse | true
            PUT | /api/v1/films/{id} | updateFilm | 200 | UpdateFilmRequest | ApiResponseFilmResponse | true
            PUT | /api/v1/films/{id}/genres/{genreId} | addFilmGenre | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/films/{id}/links/{linkId} | updateFilmLink | 200 | UpdateFilmLinkRequest | ApiResponseFilmLinkResponse | true
            PUT | /api/v1/films/{id}/story-archetypes/{storyArchetypeId} | addFilmStoryArchetype | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/films/{id}/world-settings/{worldSettingId} | addFilmWorldSetting | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/finance/categories/{id} | updateTransactionCategory | 200 | UpdateTransactionCategoryRequest | ApiResponseTransactionCategoryResponse | true
            PUT | /api/v1/finance/recurring-rules/{id} | updateRecurringRule | 200 | UpdateRecurringTransactionRuleRequest | ApiResponseRecurringTransactionRuleResponse | true
            PUT | /api/v1/finance/subscriptions/{id} | updateSubscription | 200 | UpdateSubscriptionRequest | ApiResponseSubscriptionResponse | true
            PUT | /api/v1/finance/transactions/{id} | updateFinancialTransaction | 200 | UpdateFinancialTransactionRequest | ApiResponseFinancialTransactionResponse | true
            PUT | /api/v1/finance/wallets/{id} | updateWallet | 200 | UpdateWalletRequest | ApiResponseWalletResponse | true
            PUT | /api/v1/images/{id} | updateImage | 200 | UpdateImageRequest | ApiResponseImageResponse | true
            PUT | /api/v1/journal/diary-entries/{id} | updateDiaryEntry | 200 | UpdateDiaryEntryRequest | ApiResponseDiaryEntryResponse | true
            PUT | /api/v1/knowledge/information/{id} | updateInformationItem | 200 | UpdateKnowledgeInformationRequest | ApiResponseKnowledgeInformationResponse | true
            PUT | /api/v1/knowledge/notes/{id} | updateNote | 200 | UpdateKnowledgeNoteRequest | ApiResponseKnowledgeNoteResponse | true
            PUT | /api/v1/knowledge/study/{id} | updateStudyItem | 200 | UpdateKnowledgeStudyRequest | ApiResponseKnowledgeStudyResponse | true
            PUT | /api/v1/knowledge/vocabulary/{id} | updateVocabularyItem | 200 | UpdateKnowledgeVocabularyRequest | ApiResponseKnowledgeVocabularyResponse | true
            PUT | /api/v1/location-categories/{id} | updateLocationCategory | 200 | UpdateLocationCategoryRequest | ApiResponseLocationCategoryResponse | true
            PUT | /api/v1/locations/{id} | updateLocation | 200 | UpdateLocationRequest | ApiResponseLocationResponse | true
            PUT | /api/v1/locations/{id}/business-hours | replaceLocationBusinessHours | 200 | ReplaceBusinessHoursScheduleRequest | ApiResponseBusinessHoursScheduleResponse | true
            PUT | /api/v1/locations/{id}/categories/{categoryId} | assignLocationCategory | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/locations/{id}/dining-service-styles/{style} | assignLocationDiningServiceStyle | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/people/{id} | updatePerson | 200 | UpdatePersonRequest | ApiResponsePersonResponse | true
            PUT | /api/v1/people/{id}/roles | addPersonRole | 200 | AddRoleRequest | ApiResponseVoid | true
            PUT | /api/v1/people/creator-groups/{id} | updateCreatorGroup | 200 | UpdateCreatorGroupRequest | ApiResponseCreatorGroupResponse | true
            PUT | /api/v1/people/creator-groups/{id}/members/{personId} | addCreatorGroupMember | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/personal/profiles/{id} | updatePersonalProfile | 200 | UpdatePersonalProfileRequest | ApiResponsePersonalProfileResponse | true
            PUT | /api/v1/settings | updateSettings | 200 | UpdateSettingsRequest | ApiResponseAppSettingsResponse | false
            PUT | /api/v1/vault/entries/{id}/favorite | favoriteVaultEntry | 200 | null | ApiResponseVoid | true
            PUT | /api/v1/vault/entries/{id}/rating | setVaultEntryRating | 200 | SetRatingRequest | ApiResponseVoid | true
            PUT | /api/v1/vault/entries/{id}/tags/{tagId} | attachVaultEntryTag | 200 | null | ApiResponseVoid | true
            """;


    @Test
    @DisplayName("OpenAPI /v3/api-docs exposes complete route matrix, accurate public/bearer security, and correct 201/200/error schemas")
    void openApiContractContainsAllRoutesAndUniqueOperationIds() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        // 1. Check OpenAPI version and info
        assertThat(root.has("openapi")).isTrue();
        assertThat(root.path("info").path("title").asText()).isEqualTo("Personal Private Vault API");

        // 2. Check security scheme
        JsonNode securitySchemes = root.path("components").path("securitySchemes");
        assertThat(securitySchemes.has("bearerAuth")).isTrue();
        JsonNode bearerScheme = securitySchemes.path("bearerAuth");
        assertThat(bearerScheme.path("type").asText()).isEqualTo("http");
        assertThat(bearerScheme.path("scheme").asText()).isEqualTo("bearer");
        assertThat(bearerScheme.path("bearerFormat").asText()).isEqualTo("JWT");

        // 3. Check error schemas in components
        JsonNode schemas = root.path("components").path("schemas");
        assertThat(schemas.has("ApiError")).isTrue();
        assertThat(schemas.has("ApiFieldError")).isTrue();
        assertThat(schemas.has("ErrorResponse")).isTrue();

        // 4. Verify all 18 route families are registered in paths
        JsonNode paths = root.path("paths");
        assertThat(paths.isObject()).isTrue();

        List<String> expectedPathPrefixes = List.of(
                "/api/v1/auth",
                "/api/v1/settings",
                "/api/v1/reference",
                "/api/v1/vault",
                "/api/v1/people",
                "/api/v1/fictions",
                "/api/v1/fiction-genres",
                "/api/v1/films",
                "/api/v1/film-genres",
                "/api/v1/albums",
                "/api/v1/images",
                "/api/v1/locations",
                "/api/v1/brands",
                "/api/v1/addresses",
                "/api/v1/location-categories",
                "/api/v1/knowledge",
                "/api/v1/collection",
                "/api/v1/accounts",
                "/api/v1/feed",
                "/api/v1/saved-resources",
                "/api/v1/imports/jobs",
                "/api/v1/finance",
                "/api/v1/journal",
                "/api/v1/personal",
                "/api/v1/search",
                "/api/v1/portability"
        );

        for (String prefix : expectedPathPrefixes) {
            boolean found = false;
            Iterator<String> pathNames = paths.fieldNames();
            while (pathNames.hasNext()) {
                String path = pathNames.next();
                if (path.startsWith(prefix)) {
                    found = true;
                    break;
                }
            }
            assertThat(found).as("Expected OpenAPI paths to contain prefix '%s'", prefix).isTrue();
        }

        // 5. Verify security and response codes on operations
        List<String> operationIds = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();

        Iterator<String> pathNames = paths.fieldNames();
        while (pathNames.hasNext()) {
            String path = pathNames.next();
            JsonNode pathItem = paths.path(path);
            Iterator<String> methods = pathItem.fieldNames();
            while (methods.hasNext()) {
                String method = methods.next();
                if (Set.of("get", "post", "put", "delete", "patch").contains(method.toLowerCase())) {
                    JsonNode operation = pathItem.path(method);

                    // Operation ID uniqueness
                    if (operation.has("operationId")) {
                        String opId = operation.path("operationId").asText();
                        operationIds.add(opId);
                        boolean added = uniqueIds.add(opId);
                        assertThat(added).as("Duplicate operationId '%s' found for path %s %s", opId, method.toUpperCase(), path).isTrue();
                    }

                    // Security: public paths must have explicit security: []
                    if (PUBLIC_AUTH_PATHS.contains(path)) {
                        assertThat(operation.has("security")).as("Public path %s %s must declare explicit security", method, path).isTrue();
                        assertThat(operation.path("security").isArray()).isTrue();
                        assertThat(operation.path("security").isEmpty()).as("Public path %s %s must have security: []", method, path).isTrue();
                    }

                    // Status codes
                    JsonNode responses = operation.path("responses");
                    assertThat(responses.isObject()).isTrue();

                    // All operations must document 400 and 500
                    assertThat(responses.has("400")).as("Operation %s %s must document 400", method, path).isTrue();
                    assertThat(responses.has("500")).as("Operation %s %s must document 500", method, path).isTrue();

                    // Protected paths must document 401
                    if (!PUBLIC_AUTH_PATHS.contains(path)) {
                        assertThat(responses.has("401")).as("Protected operation %s %s must document 401", method, path).isTrue();
                    }
                }
            }
        }

        assertThat(operationIds).hasSize(214);
        assertThat(uniqueIds).hasSize(214);

        // 6. Verify specific creation endpoints document 201 Created and NOT 200
        JsonNode filmGenresPost = paths.path("/api/v1/film-genres").path("post");
        assertThat(filmGenresPost.path("responses").has("201")).as("POST /api/v1/film-genres must document 201").isTrue();
        assertThat(filmGenresPost.path("responses").has("200")).as("POST /api/v1/film-genres must not document 200").isFalse();

        JsonNode bootstrapPost = paths.path("/api/v1/auth/bootstrap").path("post");
        assertThat(bootstrapPost.path("responses").has("201")).as("POST /api/v1/auth/bootstrap must document 201").isTrue();

        JsonNode loginPost = paths.path("/api/v1/auth/login").path("post");
        assertThat(loginPost.path("responses").has("200")).as("POST /api/v1/auth/login must document 200").isTrue();
        assertThat(loginPost.path("responses").has("401")).as("POST /api/v1/auth/login must document 401").isTrue();

        // Saved-resource creation endpoints document 201 and NOT 200 (FR13-2)
        JsonNode savedResourceManualPost = paths.path("/api/v1/saved-resources/manual").path("post");
        assertThat(savedResourceManualPost.path("responses").has("201")).as("POST /api/v1/saved-resources/manual must document 201").isTrue();
        assertThat(savedResourceManualPost.path("responses").has("200")).as("POST /api/v1/saved-resources/manual must not document 200").isFalse();

        JsonNode savedResourceFeedItemPost = paths.path("/api/v1/saved-resources/feed-item").path("post");
        assertThat(savedResourceFeedItemPost.path("responses").has("201")).as("POST /api/v1/saved-resources/feed-item must document 201").isTrue();
        assertThat(savedResourceFeedItemPost.path("responses").has("200")).as("POST /api/v1/saved-resources/feed-item must not document 200").isFalse();

        // Collection music credits is PUT, not POST (FR13-5)
        JsonNode musicCreditsPath = paths.path("/api/v1/collection/music/{id}/credits");
        assertThat(musicCreditsPath.has("put")).as("/api/v1/collection/music/{id}/credits must expose PUT").isTrue();
        assertThat(musicCreditsPath.has("post")).as("/api/v1/collection/music/{id}/credits must NOT expose POST").isFalse();

        // Non-CRUD action with domain errors documents 422 (FR13-2)
        JsonNode vocabReviewsPost = paths.path("/api/v1/knowledge/vocabulary/{id}/reviews").path("post");
        assertThat(vocabReviewsPost.path("responses").has("422")).as("POST /api/v1/knowledge/vocabulary/{id}/reviews must document 422").isTrue();

        // Vault metadata mutations document 409 and 422 (FR13-2 remediation)
        JsonNode favoritePut = paths.path("/api/v1/vault/entries/{id}/favorite").path("put");
        assertThat(favoritePut.path("responses").has("409")).as("PUT /api/v1/vault/entries/{id}/favorite must document 409").isTrue();
        assertThat(favoritePut.path("responses").has("422")).as("PUT /api/v1/vault/entries/{id}/favorite must document 422").isTrue();

        JsonNode favoriteDelete = paths.path("/api/v1/vault/entries/{id}/favorite").path("delete");
        assertThat(favoriteDelete.path("responses").has("409")).as("DELETE /api/v1/vault/entries/{id}/favorite must document 409").isTrue();
        assertThat(favoriteDelete.path("responses").has("422")).as("DELETE /api/v1/vault/entries/{id}/favorite must document 422").isTrue();

        JsonNode ratingPut = paths.path("/api/v1/vault/entries/{id}/rating").path("put");
        assertThat(ratingPut.path("responses").has("409")).as("PUT /api/v1/vault/entries/{id}/rating must document 409").isTrue();
        assertThat(ratingPut.path("responses").has("422")).as("PUT /api/v1/vault/entries/{id}/rating must document 422").isTrue();

        JsonNode ratingDelete = paths.path("/api/v1/vault/entries/{id}/rating").path("delete");
        assertThat(ratingDelete.path("responses").has("409")).as("DELETE /api/v1/vault/entries/{id}/rating must document 409").isTrue();
        assertThat(ratingDelete.path("responses").has("422")).as("DELETE /api/v1/vault/entries/{id}/rating must document 422").isTrue();

        JsonNode attachTagPut = paths.path("/api/v1/vault/entries/{id}/tags/{tagId}").path("put");
        assertThat(attachTagPut.path("responses").has("409")).as("PUT /api/v1/vault/entries/{id}/tags/{tagId} must document 409").isTrue();
        assertThat(attachTagPut.path("responses").has("422")).as("PUT /api/v1/vault/entries/{id}/tags/{tagId} must document 422").isTrue();

        JsonNode detachTagDelete = paths.path("/api/v1/vault/entries/{id}/tags/{tagId}").path("delete");
        assertThat(detachTagDelete.path("responses").has("409")).as("DELETE /api/v1/vault/entries/{id}/tags/{tagId} must document 409").isTrue();
        assertThat(detachTagDelete.path("responses").has("422")).as("DELETE /api/v1/vault/entries/{id}/tags/{tagId} must document 422").isTrue();

        JsonNode createTagPost = paths.path("/api/v1/vault/tags").path("post");
        assertThat(createTagPost.path("responses").has("409")).as("POST /api/v1/vault/tags must document 409").isTrue();
        assertThat(createTagPost.path("responses").has("422")).as("POST /api/v1/vault/tags must document 422").isTrue();

        // Settings GET documents 404 (FR13-2)
        JsonNode settingsGet = paths.path("/api/v1/settings").path("get");
        assertThat(settingsGet.path("responses").has("404")).as("GET /api/v1/settings must document 404").isTrue();

        // Phase 14 additions
        JsonNode imageUploadPost = paths.path("/api/v1/images/upload").path("post");
        assertThat(imageUploadPost.path("responses").has("201")).as("POST /api/v1/images/upload must document 201").isTrue();
        assertThat(imageUploadPost.path("responses").has("200")).as("POST /api/v1/images/upload must NOT document 200").isFalse();

        JsonNode imageContentGet = paths.path("/api/v1/images/{id}/content").path("get");
        assertThat(imageContentGet.path("responses").has("409")).as("GET /api/v1/images/{id}/content must document 409").isTrue();

        JsonNode exportArchivePost = paths.path("/api/v1/portability/exports").path("post");
        assertThat(exportArchivePost.path("responses").has("200")).as("POST /api/v1/portability/exports must document 200").isTrue();
        assertThat(exportArchivePost.path("responses").has("409")).as("POST /api/v1/portability/exports must NOT document 409").isFalse();
        assertThat(exportArchivePost.path("responses").has("422")).as("POST /api/v1/portability/exports must NOT document 422").isFalse();

        // 6b. Verify exact 214-endpoint method/path/operation/status/DTO contract manifest (FR13-6 + Phase 14)
        List<ExpectedRouteContract> expectedContracts = ACCEPTED_MANIFEST.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(line -> {
                    String[] parts = line.split("\\s*\\|\\s*");
                    return new ExpectedRouteContract(
                            parts[0],
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            "null".equals(parts[4]) ? null : parts[4],
                            parts[5],
                            Boolean.parseBoolean(parts[6])
                    );
                })
                .toList();

        assertThat(expectedContracts).as("Accepted manifest must specify exactly 214 endpoints").hasSize(214);

        int totalOperationsInOpenApi = 0;
        Iterator<String> pathKeyIterator = paths.fieldNames();
        while (pathKeyIterator.hasNext()) {
            String pKey = pathKeyIterator.next();
            JsonNode pItem = paths.path(pKey);
            Iterator<String> mIterator = pItem.fieldNames();
            while (mIterator.hasNext()) {
                String mKey = mIterator.next().toLowerCase();
                if (Set.of("get", "post", "put", "delete", "patch").contains(mKey)) {
                    totalOperationsInOpenApi++;
                }
            }
        }
        assertThat(totalOperationsInOpenApi).as("Total OpenAPI operations must match exact 214 manifest count").isEqualTo(214);

        for (ExpectedRouteContract contract : expectedContracts) {
            JsonNode pathItem = paths.path(contract.path());
            assertThat(pathItem.isMissingNode()).as("Missing path %s", contract.path()).isFalse();

            JsonNode op = pathItem.path(contract.method().toLowerCase());
            assertThat(op.isMissingNode()).as("Missing method %s on path %s", contract.method(), contract.path()).isFalse();

            assertThat(op.path("operationId").asText())
                    .as("Operation ID mismatch on %s %s", contract.method(), contract.path())
                    .isEqualTo(contract.operationId());

            assertThat(op.path("responses").has(String.valueOf(contract.successStatus())))
                    .as("Expected success status %d missing on %s %s", contract.successStatus(), contract.method(), contract.path())
                    .isTrue();

            if (contract.successStatus() == 201) {
                assertThat(op.path("responses").has("200"))
                        .as("200 OK must NOT be documented on creation operation %s %s", contract.method(), contract.path())
                        .isFalse();
            }

            assertThat(op.path("responses").has("404"))
                    .as("404 response documentation mismatch on %s %s", contract.method(), contract.path())
                    .isEqualTo(contract.expect404());

            if (contract.requestDto() != null) {
                assertThat(op.has("requestBody"))
                        .as("Missing expected requestBody on %s %s", contract.method(), contract.path())
                        .isTrue();
                if ("multipart".equals(contract.requestDto())) {
                    assertThat(op.path("requestBody").path("content").has("multipart/form-data"))
                            .as("Expected multipart/form-data on %s %s", contract.method(), contract.path())
                            .isTrue();
                } else {
                    String reqRef = op.path("requestBody").path("content").path("application/json").path("schema").path("$ref").asText();
                    assertThat(reqRef)
                            .as("Request DTO schema mismatch on %s %s", contract.method(), contract.path())
                            .isEqualTo("#/components/schemas/" + contract.requestDto());
                }
            } else {
                assertThat(op.has("requestBody"))
                        .as("Unexpected requestBody on %s %s", contract.method(), contract.path())
                        .isFalse();
            }

            if ("binary".equals(contract.responseDto())) {
                JsonNode respContent = op.path("responses").path(String.valueOf(contract.successStatus())).path("content");
                assertThat(respContent.isObject() && !respContent.isEmpty())
                        .as("Binary response content must be non-empty on %s %s", contract.method(), contract.path())
                        .isTrue();
                boolean hasBinarySchema = false;
                java.util.Iterator<String> mediaTypeNames = respContent.fieldNames();
                while (mediaTypeNames.hasNext()) {
                    String mediaTypeName = mediaTypeNames.next();
                    JsonNode mediaTypeNode = respContent.get(mediaTypeName);
                    JsonNode schemaNode = mediaTypeNode.path("schema");
                    if ("binary".equals(schemaNode.path("format").asText())
                            && "string".equals(schemaNode.path("type").asText())) {
                        if (contract.path().equals("/api/v1/portability/exports")) {
                            assertThat(mediaTypeName).isEqualTo("application/zip");
                        } else if (contract.path().equals("/api/v1/images/{id}/content")) {
                            assertThat(mediaTypeName).isEqualTo("*/*");
                        }
                        hasBinarySchema = true;
                        break;
                    }
                }
                assertThat(hasBinarySchema)
                        .as("Binary response schema (type=string, format=binary) and approved media type expected on %s %s", contract.method(), contract.path())
                        .isTrue();
            } else {
                JsonNode respContent = op.path("responses").path(String.valueOf(contract.successStatus())).path("content");
                String respRef = respContent.has("*/*")
                        ? respContent.path("*/*").path("schema").path("$ref").asText()
                        : respContent.path("application/json").path("schema").path("$ref").asText();
                assertThat(respRef)
                        .as("Response DTO schema mismatch on %s %s", contract.method(), contract.path())
                        .isEqualTo("#/components/schemas/" + contract.responseDto());
            }

            if (contract.path().equals("/api/v1/images/upload")) {
                JsonNode responsesNode = op.path("responses");
                assertThat(responsesNode.hasNonNull("413"))
                        .as("POST /api/v1/images/upload must document 413 Payload Too Large response")
                        .isTrue();
                assertThat(responsesNode.path("400").path("content").path("application/json").path("schema").path("$ref").asText())
                        .as("POST /api/v1/images/upload 400 schema must be ErrorResponse")
                        .isEqualTo("#/components/schemas/ErrorResponse");
                assertThat(responsesNode.path("413").path("content").path("application/json").path("schema").path("$ref").asText())
                        .as("POST /api/v1/images/upload 413 schema must be ErrorResponse")
                        .isEqualTo("#/components/schemas/ErrorResponse");
                JsonNode reqContent = op.path("requestBody").path("content");
                assertThat(reqContent.hasNonNull("multipart/form-data"))
                        .as("POST /api/v1/images/upload must consume multipart/form-data")
                        .isTrue();
                JsonNode fileSchema = reqContent.path("multipart/form-data").path("schema").path("properties").path("file");
                assertThat(fileSchema.path("type").asText()).isEqualTo("string");
                assertThat(fileSchema.path("format").asText()).isEqualTo("binary");
            }
        }

        // 7. Excluded routes must not exist in OpenAPI
        // Standalone vault create: POST /api/v1/vault/entries
        JsonNode vaultEntriesPath = paths.path("/api/v1/vault/entries");
        if (vaultEntriesPath.isObject()) {
            assertThat(vaultEntriesPath.has("post")).as("POST /api/v1/vault/entries must not exist").isFalse();
        }

        // Feed ingestFetch: no /ingest-fetch or /fetch
        Iterator<String> allPaths = paths.fieldNames();
        while (allPaths.hasNext()) {
            String p = allPaths.next();
            assertThat(p).doesNotContain("ingest-fetch");
            assertThat(p).doesNotContain("/fetch");
            assertThat(p).doesNotMatch("^/api/v1/(people|fictions|films|albums|locations|accounts|feed|imports|finance|journal|personal)/search$");
        }
    }
}
