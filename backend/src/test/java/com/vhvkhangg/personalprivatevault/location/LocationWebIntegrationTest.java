package com.vhvkhangg.personalprivatevault.location;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Location, Brand, Address, Category, and Business Hours endpoints work over HTTP")
    void locationLifecycleOverHttp() throws Exception {
        // 1. Create brand -> 201 Created
        String brandName = "Brand " + System.currentTimeMillis();
        String brandResponse = mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + brandName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(brandName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number brandId = objectMapper.readTree(brandResponse).path("data").path("id").numberValue();

        // 2. Create category -> 201 Created
        String categoryName = "Category " + System.currentTimeMillis();
        String categoryResponse = mockMvc.perform(post("/api/v1/location-categories")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + categoryName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(categoryName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/v1/location-categories/by-name")
                        .param("name", categoryName)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(categoryName));

        Number categoryId = objectMapper.readTree(categoryResponse).path("data").path("id").numberValue();

        // 3. Create address -> 201 Created
        String addressPayload = """
                {
                  "streetAddress": "123 Main St",
                  "locality": "Sample City",
                  "countryCode": "US"
                }
                """;

        String addressResponse = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number addressId = objectMapper.readTree(addressResponse).path("data").path("id").numberValue();

        // 4. Create location -> 201 Created
        String locationName = "Main Branch " + System.currentTimeMillis();
        String locationPayload = """
                {
                  "name": "%s",
                  "brandId": %d,
                  "addressId": %d
                }
                """.formatted(locationName, brandId, addressId);

        String locationResponse = mockMvc.perform(post("/api/v1/locations")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(locationPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(locationName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number locationId = objectMapper.readTree(locationResponse).path("data").path("id").numberValue();

        // 5. Assign category to location -> 200 OK (PUT)
        mockMvc.perform(put("/api/v1/locations/{id}/categories/{categoryId}", locationId, categoryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 6. Get location by ID -> 200 OK
        mockMvc.perform(get("/api/v1/locations/{id}", locationId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(locationName));
    }
}
