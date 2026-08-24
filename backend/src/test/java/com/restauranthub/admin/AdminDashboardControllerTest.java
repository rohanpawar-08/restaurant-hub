package com.restauranthub.admin;

import com.restauranthub.admin.dto.DashboardSummaryResponse;
import com.restauranthub.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminDashboardService adminDashboardService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminDashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/summary should return 200 OK with summary metrics")
    void shouldReturnDashboardSummary() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                10L,
                2L,
                3L,
                1L,
                1L,
                15L,
                20L,
                18L
        );

        when(adminDashboardService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.confirmedOrders").value(2))
                .andExpect(jsonPath("$.preparingOrders").value(3))
                .andExpect(jsonPath("$.readyOrders").value(1))
                .andExpect(jsonPath("$.outForDeliveryOrders").value(1))
                .andExpect(jsonPath("$.totalCustomers").value(15))
                .andExpect(jsonPath("$.totalFoods").value(20))
                .andExpect(jsonPath("$.activeFoods").value(18));
    }
}
