package com.restauranthub.settings;

import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RestaurantSettingsIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantSettingsRepository settingsRepository;

    private User customerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        customerUser = userRepository.findByEmailIgnoreCase("settings.customer@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Regular Customer",
                        "settings.customer@example.com",
                        "9876540001",
                        "password123",
                        UserRole.CUSTOMER
                )));

        adminUser = userRepository.findByEmailIgnoreCase("settings.admin@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Admin User",
                        "settings.admin@example.com",
                        "9876540002",
                        "password123",
                        UserRole.ADMIN
                )));
    }

    @Test
    @DisplayName("Public GET /api/v1/settings should return active settings without authentication")
    void publicGetSettingsShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName", is("RestaurantHub")))
                .andExpect(jsonPath("$.currencyCode", is("INR")))
                .andExpect(jsonPath("$.currencySymbol", is("₹")))
                .andExpect(jsonPath("$.deliveryFee", notNullValue()))
                .andExpect(jsonPath("$.freeDeliveryThreshold", notNullValue()))
                .andExpect(jsonPath("$.acceptingOrders", is(true)));
    }

    @Test
    @DisplayName("Unauthenticated PUT /api/v1/admin/settings should return 401 Unauthorized")
    void unauthenticatedAdminPutSettingsShouldFail() throws Exception {
        String json = createValidUpdateJson();

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Customer PUT /api/v1/admin/settings should return 403 Forbidden")
    void customerPutSettingsShouldBeForbidden() throws Exception {
        String json = createValidUpdateJson();

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(customerUser.getEmail()).roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin PUT /api/v1/admin/settings without CSRF token should return 403 Forbidden")
    void adminPutSettingsWithoutCsrfShouldFail() throws Exception {
        String json = createValidUpdateJson();

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin PUT /api/v1/admin/settings with CSRF should succeed and update settings")
    void adminPutSettingsWithCsrfShouldSucceed() throws Exception {
        String json = """
                {
                    "restaurantName": "Royal Spice Bistro",
                    "tagline": "Authentic Indian Cuisine",
                    "phone": "9876543299",
                    "email": "info@royalspice.com",
                    "addressLine1": "45 Palace Road",
                    "addressLine2": "Opposite Fort",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pinCode": "560001",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": 50.00,
                    "freeDeliveryThreshold": 600.00,
                    "estimatedDeliveryMinutes": 40,
                    "gstin": "29ABCDE1234F1Z5",
                    "fssaiNumber": "12345678901234",
                    "openingTime": "10:00:00",
                    "closingTime": "22:30:00",
                    "acceptingOrders": true,
                    "logoUrl": "https://cdn.example.com/logo.png",
                    "heroImageUrl": "https://cdn.example.com/hero.png",
                    "primaryColor": "#FF5500",
                    "secondaryColor": "#0F172A"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName", is("Royal Spice Bistro")))
                .andExpect(jsonPath("$.tagline", is("Authentic Indian Cuisine")))
                .andExpect(jsonPath("$.phone", is("9876543299")))
                .andExpect(jsonPath("$.email", is("info@royalspice.com")))
                .andExpect(jsonPath("$.city", is("Bengaluru")))
                .andExpect(jsonPath("$.pinCode", is("560001")))
                .andExpect(jsonPath("$.deliveryFee", is(50.00)))
                .andExpect(jsonPath("$.freeDeliveryThreshold", is(600.00)))
                .andExpect(jsonPath("$.estimatedDeliveryMinutes", is(40)))
                .andExpect(jsonPath("$.gstin", is("29ABCDE1234F1Z5")))
                .andExpect(jsonPath("$.fssaiNumber", is("12345678901234")))
                .andExpect(jsonPath("$.primaryColor", is("#FF5500")))
                .andExpect(jsonPath("$.secondaryColor", is("#0F172A")));
    }

    @Test
    @DisplayName("Validation: Should reject invalid Indian PIN code")
    void shouldRejectInvalidPinCode() throws Exception {
        String invalidJson = """
                {
                    "restaurantName": "Royal Spice Bistro",
                    "tagline": "Authentic Indian Cuisine",
                    "phone": "9876543299",
                    "email": "info@royalspice.com",
                    "addressLine1": "45 Palace Road",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pinCode": "1234",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": 50.00,
                    "freeDeliveryThreshold": 600.00,
                    "estimatedDeliveryMinutes": 35,
                    "acceptingOrders": true
                }
                """;

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.pinCode", containsString("6-digit")));
    }

    @Test
    @DisplayName("Validation: Should reject invalid Indian phone number")
    void shouldRejectInvalidPhoneNumber() throws Exception {
        String invalidJson = """
                {
                    "restaurantName": "Royal Spice Bistro",
                    "tagline": "Authentic Indian Cuisine",
                    "phone": "12345",
                    "email": "info@royalspice.com",
                    "addressLine1": "45 Palace Road",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pinCode": "560001",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": 50.00,
                    "freeDeliveryThreshold": 600.00,
                    "estimatedDeliveryMinutes": 35,
                    "acceptingOrders": true
                }
                """;

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.phone", containsString("Indian mobile number")));
    }

    @Test
    @DisplayName("Validation: Should reject negative delivery fee")
    void shouldRejectNegativeDeliveryFee() throws Exception {
        String invalidJson = """
                {
                    "restaurantName": "Royal Spice Bistro",
                    "tagline": "Authentic Indian Cuisine",
                    "phone": "9876543299",
                    "email": "info@royalspice.com",
                    "addressLine1": "45 Palace Road",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pinCode": "560001",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": -10.00,
                    "freeDeliveryThreshold": 600.00,
                    "estimatedDeliveryMinutes": 35,
                    "acceptingOrders": true
                }
                """;

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.deliveryFee", containsString("cannot be negative")));
    }

    @Test
    @DisplayName("Validation: Should reject invalid Hex color format")
    void shouldRejectInvalidHexColor() throws Exception {
        String invalidJson = """
                {
                    "restaurantName": "Royal Spice Bistro",
                    "tagline": "Authentic Indian Cuisine",
                    "phone": "9876543299",
                    "email": "info@royalspice.com",
                    "addressLine1": "45 Palace Road",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pinCode": "560001",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": 50.00,
                    "freeDeliveryThreshold": 600.00,
                    "estimatedDeliveryMinutes": 35,
                    "acceptingOrders": true,
                    "primaryColor": "blue"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/settings")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.primaryColor", containsString("Hex color code")));
    }

    @Test
    @DisplayName("Media Endpoint: Unauthenticated image upload should return 401")
    void unauthenticatedMediaUploadShouldFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "food.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0}
        );

        mockMvc.perform(multipart("/api/v1/admin/media/images")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Media Endpoint: Customer image upload should return 403")
    void customerMediaUploadShouldBeForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "food.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0}
        );

        mockMvc.perform(multipart("/api/v1/admin/media/images")
                        .file(file)
                        .with(user(customerUser.getEmail()).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Media Endpoint: Should reject SVG and executable files with 400")
    void shouldRejectDangerousImageFormats() throws Exception {
        MockMultipartFile svgFile = new MockMultipartFile(
                "file",
                "malicious.svg",
                "image/svg+xml",
                "<svg><script>alert(1)</script></svg>".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/media/images")
                        .file(svgFile)
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Media Endpoint: Valid image upload when storage is unconfigured should return 503")
    void validImageUploadWhenStorageNotConfiguredShouldReturn503() throws Exception {
        MockMultipartFile validImage = new MockMultipartFile(
                "file",
                "dish.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0}
        );

        mockMvc.perform(multipart("/api/v1/admin/media/images")
                        .file(validImage)
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Media Endpoint: Status endpoint should return provider information")
    void mediaStatusShouldReturnInformation() throws Exception {
        mockMvc.perform(get("/api/v1/admin/media/status")
                        .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", notNullValue()))
                .andExpect(jsonPath("$.provider", notNullValue()));
    }

    private String createValidUpdateJson() {
        return """
                {
                    "restaurantName": "RestaurantHub",
                    "tagline": "Fresh food, delivered with care",
                    "phone": "9876543210",
                    "email": "contact@restauranthub.com",
                    "addressLine1": "123 Gourmet Boulevard",
                    "addressLine2": "Near City Center",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "pinCode": "400001",
                    "currencyCode": "INR",
                    "currencySymbol": "₹",
                    "deliveryFee": 40.00,
                    "freeDeliveryThreshold": 500.00,
                    "estimatedDeliveryMinutes": 35,
                    "openingTime": "09:00:00",
                    "closingTime": "23:00:00",
                    "acceptingOrders": true,
                    "primaryColor": "#FF6B00",
                    "secondaryColor": "#1E293B"
                }
                """;
    }
}
