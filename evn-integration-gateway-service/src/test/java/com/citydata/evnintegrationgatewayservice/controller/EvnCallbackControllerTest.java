package com.citydata.evnintegrationgatewayservice.controller;

import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.citydata.evnintegrationgatewayservice.service.EvnCallbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvnCallbackController.class)
class EvnCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvnCallbackService evnCallbackService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void receiveCallback_ShouldReturnOk_WhenValidRequest() throws Exception {
        ElectricityUsageEvent event = new ElectricityUsageEvent();
        event.setCustomerId("12345");
        event.setConsumption(350.2);
        event.setBillingCycle("2025-02");

        doNothing().when(evnCallbackService).processCallback(Mockito.any(ElectricityUsageEvent.class));

        mockMvc.perform(post("/evn/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }

    @Test
    void receiveCallback_ShouldReturnInternalServerError_WhenProcessingFails() throws Exception {
        ElectricityUsageEvent event = new ElectricityUsageEvent();
        event.setCustomerId("12345");
        event.setConsumption(350.2);
        event.setBillingCycle("2025-02");

        doThrow(new JsonProcessingException("Test Exception") {}).when(evnCallbackService)
                .processCallback(Mockito.any(ElectricityUsageEvent.class));

        mockMvc.perform(post("/evn/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isInternalServerError());
    }
}
