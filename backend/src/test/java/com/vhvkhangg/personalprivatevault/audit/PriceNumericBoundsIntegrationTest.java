package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.CreateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.InvalidShoppingItemException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.UpdateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;
import com.vhvkhangg.personalprivatevault.collection.software.software.CreateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.software.InvalidSoftwareItemException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareOperations;
import com.vhvkhangg.personalprivatevault.collection.software.software.UpdateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.brand.BrandOperations;
import com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException;
import com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationRequest;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-12 regression test:
 * Verifies that price validators for Brand, Location, Study, Shopping, and Software reject
 * numeric(19,4) width/scale bounds violations before persistence without rounding or normalization,
 * verifies reload equality for MAX_VALID across create and update, and verifies HTTP boundary behavior.
 */
class PriceNumericBoundsIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private BrandOperations brandOperations;

    @Autowired
    private LocationOperations locationOperations;

    @Autowired
    private AddressOperations addressOperations;

    @Autowired
    private StudyItemOperations studyOperations;

    @Autowired
    private ShoppingOperations shoppingOperations;

    @Autowired
    private SoftwareOperations softwareOperations;

    private static final BigDecimal FIVE_DECIMALS = new BigDecimal("10.12345");
    private static final BigDecimal OVERFLOW_VALUE = new BigDecimal("1000000000000000.0000"); // 16 integer digits
    private static final BigDecimal MAX_VALID = new BigDecimal("999999999999999.9999"); // 15 int + 4 frac digits
    private static final BigDecimal FOUR_DECIMALS = new BigDecimal("10.1234");
    private final com.fasterxml.jackson.databind.ObjectMapper decimalMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    @Test
    @DisplayName("BA15-12: Brand rejects price with scale > 4 and width overflow on create and update, preserves MAX_VALID")
    void brandRejectsPriceViolationsAndPreservesMaxValid() {
        // Create rejection on minPrice
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand("Brand A", null, null, null, FIVE_DECIMALS, null, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand("Brand B", null, null, null, OVERFLOW_VALUE, null, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("exceeds maximum");

        // Create rejection on independent maxPrice
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand("Brand C", null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand("Brand D", null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("exceeds maximum");

        // Create with MAX_VALID and assert reloaded equality
        var createdMax = brandOperations.create(new CreateBrandCommand("Brand Max", null, null, null, MAX_VALID, MAX_VALID, "USD", null));
        var reloadedMax = brandOperations.findById(createdMax.id());
        assertThat(reloadedMax.minPrice()).isEqualByComparingTo(MAX_VALID);
        assertThat(reloadedMax.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // Update rejection on minPrice
        assertThatThrownBy(() -> brandOperations.update(new UpdateBrandCommand(createdMax.id(), "Brand Max", null, null, null, FIVE_DECIMALS, null, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> brandOperations.update(new UpdateBrandCommand(createdMax.id(), "Brand Max", null, null, null, OVERFLOW_VALUE, null, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("exceeds maximum");

        // Update rejection on independent maxPrice
        assertThatThrownBy(() -> brandOperations.update(new UpdateBrandCommand(createdMax.id(), "Brand Max", null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> brandOperations.update(new UpdateBrandCommand(createdMax.id(), "Brand Max", null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null)))
                .isInstanceOf(InvalidBrandException.class)
                .hasMessageContaining("exceeds maximum");

        // Update with 4 decimals succeeds
        var updated = brandOperations.update(new UpdateBrandCommand(createdMax.id(), "Brand Max", null, null, null, FOUR_DECIMALS, FOUR_DECIMALS, "USD", null));
        assertThat(updated.minPrice()).isEqualByComparingTo(FOUR_DECIMALS);
    }

    @Test
    @DisplayName("BA15-12: Location rejects price with scale > 4 and width overflow on create and update, preserves MAX_VALID")
    void locationRejectsPriceViolationsAndPreservesMaxValid() {
        var addr = addressOperations.create(new CreateAddressCommand("Addr", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));

        // Create rejection on minPrice
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(null, addr.id(), "Loc A", null, null, null, null, FIVE_DECIMALS, null, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(null, addr.id(), "Loc B", null, null, null, null, OVERFLOW_VALUE, null, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("exceeds maximum");

        // Create rejection on independent maxPrice
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(null, addr.id(), "Loc C", null, null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(null, addr.id(), "Loc D", null, null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("exceeds maximum");

        // Create with MAX_VALID and assert reloaded equality
        var createdMax = locationOperations.create(new CreateLocationCommand(null, addr.id(), "Loc Max", null, null, null, null, MAX_VALID, MAX_VALID, "USD", null));
        var reloadedMax = locationOperations.findById(createdMax.id());
        assertThat(reloadedMax.minPrice()).isEqualByComparingTo(MAX_VALID);
        assertThat(reloadedMax.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // Update rejection on minPrice
        assertThatThrownBy(() -> locationOperations.update(new UpdateLocationCommand(createdMax.id(), null, addr.id(), "Loc Max", null, null, null, null, FIVE_DECIMALS, null, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> locationOperations.update(new UpdateLocationCommand(createdMax.id(), null, addr.id(), "Loc Max", null, null, null, null, OVERFLOW_VALUE, null, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("exceeds maximum");

        // Update rejection on independent maxPrice
        assertThatThrownBy(() -> locationOperations.update(new UpdateLocationCommand(createdMax.id(), null, addr.id(), "Loc Max", null, null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> locationOperations.update(new UpdateLocationCommand(createdMax.id(), null, addr.id(), "Loc Max", null, null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null)))
                .isInstanceOf(InvalidLocationException.class)
                .hasMessageContaining("exceeds maximum");

        // Update with 4 decimals succeeds
        var updated = locationOperations.update(new UpdateLocationCommand(createdMax.id(), null, addr.id(), "Loc Max", null, null, null, null, FOUR_DECIMALS, FOUR_DECIMALS, "USD", null));
        assertThat(updated.minPrice()).isEqualByComparingTo(FOUR_DECIMALS);
    }

    @Test
    @DisplayName("BA15-12: StudyItem rejects price with scale > 4 and width overflow on create and update, preserves MAX_VALID")
    void studyItemRejectsPriceViolationsAndPreservesMaxValid() {
        // Create rejection
        assertThatThrownBy(() -> studyOperations.create(new CreateStudyItemCommand("Study A", null, StudyType.BOOK, null, null, null, null, null, FIVE_DECIMALS, "USD", null, null, null, null, null, null)))
                .isInstanceOf(InvalidStudyItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> studyOperations.create(new CreateStudyItemCommand("Study B", null, StudyType.BOOK, null, null, null, null, null, OVERFLOW_VALUE, "USD", null, null, null, null, null, null)))
                .isInstanceOf(InvalidStudyItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Create with MAX_VALID and assert reloaded equality
        var createdMax = studyOperations.create(new CreateStudyItemCommand("Study Max", null, StudyType.BOOK, null, null, null, null, null, MAX_VALID, "USD", null, null, null, null, null, null));
        var reloadedMax = studyOperations.findById(createdMax.id()).orElseThrow();
        assertThat(reloadedMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // Update rejection
        assertThatThrownBy(() -> studyOperations.update(createdMax.id(), new UpdateStudyItemCommand("Study Max", null, StudyType.BOOK, null, null, null, null, null, FIVE_DECIMALS, "USD", null, null, null, null, null, null)))
                .isInstanceOf(InvalidStudyItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> studyOperations.update(createdMax.id(), new UpdateStudyItemCommand("Study Max", null, StudyType.BOOK, null, null, null, null, null, OVERFLOW_VALUE, "USD", null, null, null, null, null, null)))
                .isInstanceOf(InvalidStudyItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Update with 4 decimals succeeds
        var updated = studyOperations.update(createdMax.id(), new UpdateStudyItemCommand("Study Max", null, StudyType.BOOK, null, null, null, null, null, FOUR_DECIMALS, "USD", null, null, null, null, null, null));
        assertThat(updated.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);
    }

    @Test
    @DisplayName("BA15-12: ShoppingItem rejects price with scale > 4 and width overflow on create and update, preserves MAX_VALID")
    void shoppingItemRejectsPriceViolationsAndPreservesMaxValid() {
        // Create rejection
        assertThatThrownBy(() -> shoppingOperations.create(new CreateShoppingItemCommand("Shop A", null, null, FIVE_DECIMALS, "USD", null, null, null, null)))
                .isInstanceOf(InvalidShoppingItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> shoppingOperations.create(new CreateShoppingItemCommand("Shop B", null, null, OVERFLOW_VALUE, "USD", null, null, null, null)))
                .isInstanceOf(InvalidShoppingItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Create with MAX_VALID and assert reloaded equality
        var createdMax = shoppingOperations.create(new CreateShoppingItemCommand("Shop Max", null, null, MAX_VALID, "USD", null, null, null, null));
        var reloadedMax = shoppingOperations.findById(createdMax.id()).orElseThrow();
        assertThat(reloadedMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // Update rejection
        assertThatThrownBy(() -> shoppingOperations.update(createdMax.id(), new UpdateShoppingItemCommand("Shop Max", null, null, FIVE_DECIMALS, "USD", null, null, null, null)))
                .isInstanceOf(InvalidShoppingItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> shoppingOperations.update(createdMax.id(), new UpdateShoppingItemCommand("Shop Max", null, null, OVERFLOW_VALUE, "USD", null, null, null, null)))
                .isInstanceOf(InvalidShoppingItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Update with 4 decimals succeeds
        var updated = shoppingOperations.update(createdMax.id(), new UpdateShoppingItemCommand("Shop Max", null, null, FOUR_DECIMALS, "USD", null, null, null, null));
        assertThat(updated.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);
    }

    @Test
    @DisplayName("BA15-12: SoftwareItem rejects price with scale > 4 and width overflow on create and update, preserves MAX_VALID")
    void softwareItemRejectsPriceViolationsAndPreservesMaxValid() {
        // Create rejection
        assertThatThrownBy(() -> softwareOperations.create(new CreateSoftwareItemCommand("Soft A", SoftwareType.APPLICATION, null, null, FIVE_DECIMALS, "USD", null, null)))
                .isInstanceOf(InvalidSoftwareItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> softwareOperations.create(new CreateSoftwareItemCommand("Soft B", SoftwareType.APPLICATION, null, null, OVERFLOW_VALUE, "USD", null, null)))
                .isInstanceOf(InvalidSoftwareItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Create with MAX_VALID and assert reloaded equality
        var createdMax = softwareOperations.create(new CreateSoftwareItemCommand("Soft Max", SoftwareType.APPLICATION, null, null, MAX_VALID, "USD", null, null));
        var reloadedMax = softwareOperations.findById(createdMax.id()).orElseThrow();
        assertThat(reloadedMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // Update rejection
        assertThatThrownBy(() -> softwareOperations.update(createdMax.id(), new UpdateSoftwareItemCommand("Soft Max", SoftwareType.APPLICATION, null, null, FIVE_DECIMALS, "USD", null, null)))
                .isInstanceOf(InvalidSoftwareItemException.class)
                .hasMessageContaining("scale must not exceed 4");

        assertThatThrownBy(() -> softwareOperations.update(createdMax.id(), new UpdateSoftwareItemCommand("Soft Max", SoftwareType.APPLICATION, null, null, OVERFLOW_VALUE, "USD", null, null)))
                .isInstanceOf(InvalidSoftwareItemException.class)
                .hasMessageContaining("exceeds maximum");

        // Update with 4 decimals succeeds
        var updated = softwareOperations.update(createdMax.id(), new UpdateSoftwareItemCommand("Soft Max", SoftwareType.APPLICATION, null, null, FOUR_DECIMALS, "USD", null, null));
        assertThat(updated.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);
    }

    @Test
    @DisplayName("BA15-12: HTTP boundary rejects scale > 4 and overflow on create/update, accepts MAX_VALID and verifies persisted reload")
    void httpBoundaryRejectsPriceViolationsAndAcceptsMaxValid() throws Exception {
        // --- 1. Brand HTTP ---
        // POST rejections: minPrice scale & overflow
        CreateBrandRequest brandInvalidMinScale = new CreateBrandRequest("Brand HTTP Scale", null, null, null, FIVE_DECIMALS, null, "USD", null);
        mockMvc.perform(post("/api/v1/brands")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandInvalidMinScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        CreateBrandRequest brandInvalidMinOverflow = new CreateBrandRequest("Brand HTTP Overflow", null, null, null, OVERFLOW_VALUE, null, "USD", null);
        mockMvc.perform(post("/api/v1/brands")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandInvalidMinOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        // POST rejections: independent maxPrice scale & overflow
        CreateBrandRequest brandInvalidMaxScale = new CreateBrandRequest("Brand HTTP Max Scale", null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null);
        mockMvc.perform(post("/api/v1/brands")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandInvalidMaxScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        CreateBrandRequest brandInvalidMaxOverflow = new CreateBrandRequest("Brand HTTP Max Overflow", null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null);
        mockMvc.perform(post("/api/v1/brands")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandInvalidMaxOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        // POST valid MAX_VALID
        CreateBrandRequest validMax = new CreateBrandRequest("Brand HTTP Max", null, null, null, MAX_VALID, MAX_VALID, "USD", null);
        var brandPostRes = mockMvc.perform(post("/api/v1/brands")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMax)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var brandJson = decimalMapper.readTree(brandPostRes);
        Long brandId = brandJson.path("data").path("id").asLong();
        assertThat(new BigDecimal(brandJson.path("data").path("minPrice").asText())).isEqualByComparingTo(MAX_VALID);
        assertThat(new BigDecimal(brandJson.path("data").path("maxPrice").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedBrandMax = brandOperations.findById(brandId);
        assertThat(reloadedBrandMax.minPrice()).isEqualByComparingTo(MAX_VALID);
        assertThat(reloadedBrandMax.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // PUT rejections: minPrice scale & overflow
        UpdateBrandRequest brandPutInvalidMinScale = new UpdateBrandRequest("Brand HTTP Max", null, null, null, FIVE_DECIMALS, MAX_VALID, "USD", null);
        mockMvc.perform(put("/api/v1/brands/{id}", brandId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandPutInvalidMinScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        UpdateBrandRequest brandPutInvalidMinOverflow = new UpdateBrandRequest("Brand HTTP Max", null, null, null, OVERFLOW_VALUE, MAX_VALID, "USD", null);
        mockMvc.perform(put("/api/v1/brands/{id}", brandId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandPutInvalidMinOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        // PUT rejections: maxPrice scale & overflow
        UpdateBrandRequest brandPutInvalidMaxScale = new UpdateBrandRequest("Brand HTTP Max", null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null);
        mockMvc.perform(put("/api/v1/brands/{id}", brandId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandPutInvalidMaxScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        UpdateBrandRequest brandPutInvalidMaxOverflow = new UpdateBrandRequest("Brand HTTP Max", null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null);
        mockMvc.perform(put("/api/v1/brands/{id}", brandId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandPutInvalidMaxOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BRAND_INVALID"));

        // PUT valid update with 4 decimals
        UpdateBrandRequest brandPutUpdate = new UpdateBrandRequest("Brand HTTP Max", null, null, null, FOUR_DECIMALS, MAX_VALID, "USD", null);
        var brandPutRes = mockMvc.perform(put("/api/v1/brands/{id}", brandId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandPutUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var brandPutJson = decimalMapper.readTree(brandPutRes);
        assertThat(new BigDecimal(brandPutJson.path("data").path("minPrice").asText())).isEqualByComparingTo(FOUR_DECIMALS);
        assertThat(new BigDecimal(brandPutJson.path("data").path("maxPrice").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedBrandPut = brandOperations.findById(brandId);
        assertThat(reloadedBrandPut.minPrice()).isEqualByComparingTo(FOUR_DECIMALS);
        assertThat(reloadedBrandPut.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // --- 2. Location HTTP ---
        var addr = addressOperations.create(new CreateAddressCommand("Addr Price", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));

        // POST rejections: minPrice scale & overflow
        CreateLocationRequest locInvalidMinScale = new CreateLocationRequest(brandId, addr.id(), "Loc Price", null, null, null, null, FIVE_DECIMALS, null, "USD", null);
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locInvalidMinScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        CreateLocationRequest locInvalidMinOverflow = new CreateLocationRequest(brandId, addr.id(), "Loc Price", null, null, null, null, OVERFLOW_VALUE, null, "USD", null);
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locInvalidMinOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        // POST rejections: maxPrice scale & overflow
        CreateLocationRequest locInvalidMaxScale = new CreateLocationRequest(brandId, addr.id(), "Loc Price", null, null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null);
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locInvalidMaxScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        CreateLocationRequest locInvalidMaxOverflow = new CreateLocationRequest(brandId, addr.id(), "Loc Price", null, null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null);
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locInvalidMaxOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        // POST valid MAX_VALID
        CreateLocationRequest validLoc = new CreateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, MAX_VALID, MAX_VALID, "USD", null);
        var locPostRes = mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoc)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var locJson = decimalMapper.readTree(locPostRes);
        Long locId = locJson.path("data").path("id").asLong();
        assertThat(new BigDecimal(locJson.path("data").path("minPrice").asText())).isEqualByComparingTo(MAX_VALID);
        assertThat(new BigDecimal(locJson.path("data").path("maxPrice").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedLocMax = locationOperations.findById(locId);
        assertThat(reloadedLocMax.minPrice()).isEqualByComparingTo(MAX_VALID);
        assertThat(reloadedLocMax.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // PUT rejections: minPrice scale & overflow
        UpdateLocationRequest locPutInvalidMinScale = new UpdateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, FIVE_DECIMALS, MAX_VALID, "USD", null);
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locPutInvalidMinScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        UpdateLocationRequest locPutInvalidMinOverflow = new UpdateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, OVERFLOW_VALUE, MAX_VALID, "USD", null);
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locPutInvalidMinOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        // PUT rejections: maxPrice scale & overflow
        UpdateLocationRequest locPutInvalidMaxScale = new UpdateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, FOUR_DECIMALS, FIVE_DECIMALS, "USD", null);
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locPutInvalidMaxScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        UpdateLocationRequest locPutInvalidMaxOverflow = new UpdateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, FOUR_DECIMALS, OVERFLOW_VALUE, "USD", null);
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locPutInvalidMaxOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("LOCATION_INVALID"));

        // PUT valid update with 4 decimals
        UpdateLocationRequest validLocUpdate = new UpdateLocationRequest(brandId, addr.id(), "Loc Price Max", null, null, null, null, FOUR_DECIMALS, MAX_VALID, "USD", null);
        var locPutRes = mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLocUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var locPutJson = decimalMapper.readTree(locPutRes);
        assertThat(new BigDecimal(locPutJson.path("data").path("minPrice").asText())).isEqualByComparingTo(FOUR_DECIMALS);
        assertThat(new BigDecimal(locPutJson.path("data").path("maxPrice").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedLocPut = locationOperations.findById(locId);
        assertThat(reloadedLocPut.minPrice()).isEqualByComparingTo(FOUR_DECIMALS);
        assertThat(reloadedLocPut.maxPrice()).isEqualByComparingTo(MAX_VALID);

        // --- 3. Knowledge Study HTTP ---
        // POST rejections: scale & overflow
        CreateKnowledgeStudyRequest studyInvalidScale = new CreateKnowledgeStudyRequest(
                "Study Price Scale", null, KnowledgeStudyType.BOOK, null, null, null, null, null, FIVE_DECIMALS, "USD", null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studyInvalidScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_INVALID"));

        CreateKnowledgeStudyRequest studyInvalidOverflow = new CreateKnowledgeStudyRequest(
                "Study Price Overflow", null, KnowledgeStudyType.BOOK, null, null, null, null, null, OVERFLOW_VALUE, "USD", null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studyInvalidOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_INVALID"));

        // POST valid MAX_VALID
        CreateKnowledgeStudyRequest validStudy = new CreateKnowledgeStudyRequest(
                "Study Price Max", null, KnowledgeStudyType.BOOK, null, null, null, null, null, MAX_VALID, "USD", null, null, null, null, null, null
        );
        var studyPostRes = mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStudy)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var studyJson = decimalMapper.readTree(studyPostRes);
        Long studyId = studyJson.path("data").path("id").asLong();
        assertThat(new BigDecimal(studyJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedStudyMax = studyOperations.findById(studyId).orElseThrow();
        assertThat(reloadedStudyMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // PUT rejections: scale & overflow
        UpdateKnowledgeStudyRequest invalidStudyScaleUpdate = new UpdateKnowledgeStudyRequest(
                "Study Price Max", null, KnowledgeStudyType.BOOK, null, null, null, null, null, FIVE_DECIMALS, "USD", null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudyScaleUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_INVALID"));

        UpdateKnowledgeStudyRequest invalidStudyOverflowUpdate = new UpdateKnowledgeStudyRequest(
                "Study Price Max", null, KnowledgeStudyType.BOOK, null, null, null, null, null, OVERFLOW_VALUE, "USD", null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudyOverflowUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_INVALID"));

        // PUT valid update with 4 decimals
        UpdateKnowledgeStudyRequest validStudyUpdate = new UpdateKnowledgeStudyRequest(
                "Study Price Max", null, KnowledgeStudyType.BOOK, null, null, null, null, null, FOUR_DECIMALS, "USD", null, null, null, null, null, null
        );
        var studyPutRes = mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStudyUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var studyPutJson = decimalMapper.readTree(studyPutRes);
        assertThat(new BigDecimal(studyPutJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(FOUR_DECIMALS);

        var reloadedStudyPut = studyOperations.findById(studyId).orElseThrow();
        assertThat(reloadedStudyPut.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);

        // --- 4. Collection Shopping HTTP ---
        // POST rejections: scale & overflow
        CreateShoppingItemRequest shopInvalidScale = new CreateShoppingItemRequest(
                "Shopping Price Scale", null, null, FIVE_DECIMALS, "USD", null, null, null, null
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shopInvalidScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        CreateShoppingItemRequest shopInvalidOverflow = new CreateShoppingItemRequest(
                "Shopping Price Overflow", null, null, OVERFLOW_VALUE, "USD", null, null, null, null
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shopInvalidOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        // POST valid MAX_VALID
        CreateShoppingItemRequest validShop = new CreateShoppingItemRequest(
                "Shopping Price Max", null, null, MAX_VALID, "USD", null, null, null, null
        );
        var shopPostRes = mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validShop)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var shopJson = decimalMapper.readTree(shopPostRes);
        Long shopId = shopJson.path("data").path("id").asLong();
        assertThat(new BigDecimal(shopJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedShopMax = shoppingOperations.findById(shopId).orElseThrow();
        assertThat(reloadedShopMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // PUT rejections: scale & overflow
        UpdateShoppingItemRequest invalidShopScaleUpdate = new UpdateShoppingItemRequest(
                "Shopping Price Max", null, null, FIVE_DECIMALS, "USD", null, null, null, null
        );
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidShopScaleUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        UpdateShoppingItemRequest invalidShopOverflowUpdate = new UpdateShoppingItemRequest(
                "Shopping Price Max", null, null, OVERFLOW_VALUE, "USD", null, null, null, null
        );
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidShopOverflowUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        // PUT valid update with 4 decimals
        UpdateShoppingItemRequest validShopUpdate = new UpdateShoppingItemRequest(
                "Shopping Price Max", null, null, FOUR_DECIMALS, "USD", null, null, null, null
        );
        var shopPutRes = mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validShopUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var shopPutJson = decimalMapper.readTree(shopPutRes);
        assertThat(new BigDecimal(shopPutJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(FOUR_DECIMALS);

        var reloadedShopPut = shoppingOperations.findById(shopId).orElseThrow();
        assertThat(reloadedShopPut.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);

        // --- 5. Collection Software HTTP ---
        // POST rejections: scale & overflow
        CreateSoftwareItemRequest softInvalidScale = new CreateSoftwareItemRequest(
                "Software Price Scale", CollectionSoftwareType.APPLICATION, null, null, FIVE_DECIMALS, "USD", null, null
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(softInvalidScale)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        CreateSoftwareItemRequest softInvalidOverflow = new CreateSoftwareItemRequest(
                "Software Price Overflow", CollectionSoftwareType.APPLICATION, null, null, OVERFLOW_VALUE, "USD", null, null
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(softInvalidOverflow)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        // POST valid MAX_VALID
        CreateSoftwareItemRequest validSoft = new CreateSoftwareItemRequest(
                "Software Price Max", CollectionSoftwareType.APPLICATION, null, null, MAX_VALID, "USD", null, null
        );
        var softPostRes = mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSoft)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var softJson = decimalMapper.readTree(softPostRes);
        Long softId = softJson.path("data").path("id").asLong();
        assertThat(new BigDecimal(softJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(MAX_VALID);

        var reloadedSoftMax = softwareOperations.findById(softId).orElseThrow();
        assertThat(reloadedSoftMax.priceAmount()).isEqualByComparingTo(MAX_VALID);

        // PUT rejections: scale & overflow
        UpdateSoftwareItemRequest invalidSoftScaleUpdate = new UpdateSoftwareItemRequest(
                "Software Price Max", CollectionSoftwareType.APPLICATION, null, null, FIVE_DECIMALS, "USD", null, null
        );
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSoftScaleUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        UpdateSoftwareItemRequest invalidSoftOverflowUpdate = new UpdateSoftwareItemRequest(
                "Software Price Max", CollectionSoftwareType.APPLICATION, null, null, OVERFLOW_VALUE, "USD", null, null
        );
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSoftOverflowUpdate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        // PUT valid update with 4 decimals
        UpdateSoftwareItemRequest validSoftUpdate = new UpdateSoftwareItemRequest(
                "Software Price Max", CollectionSoftwareType.APPLICATION, null, null, FOUR_DECIMALS, "USD", null, null
        );
        var softPutRes = mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSoftUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var softPutJson = decimalMapper.readTree(softPutRes);
        assertThat(new BigDecimal(softPutJson.path("data").path("priceAmount").asText())).isEqualByComparingTo(FOUR_DECIMALS);

        var reloadedSoftPut = softwareOperations.findById(softId).orElseThrow();
        assertThat(reloadedSoftPut.priceAmount()).isEqualByComparingTo(FOUR_DECIMALS);
    }
}
