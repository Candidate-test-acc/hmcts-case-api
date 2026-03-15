package uk.gov.hmcts.dts_dev_challenge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseOutcome;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseResponse;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseStatus;
import uk.gov.hmcts.dts_dev_challenge.dto.CreateCaseRequest;
import uk.gov.hmcts.dts_dev_challenge.exception.NotFoundException;
import uk.gov.hmcts.dts_dev_challenge.service.CaseService;
import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CaseController.class)
class CaseControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CaseService service;

    @Test
    void post_creates_case_and_returns_201() throws Exception {
        when(service.create(any(CreateCaseRequest.class))).thenReturn(
                new CaseResponse(
                        1L,
                        "HMCTS-2026-000001",
                        "My case",
                        "Details",
                        CaseStatus.OPEN,
                        CaseOutcome.NONE,
                        Instant.now(),
                        Instant.now(),
                        "Created case",
                        null
                )
        );

        mvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title":"My case","description":"Details"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("HMCTS-2026-000001"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void get_unknown_id_returns_404() throws Exception {
        when(service.get(eq(999999L), eq(false))).thenThrow(new NotFoundException("case not found"));

        mvc.perform(get("/api/cases/999999"))
                .andExpect(status().isNotFound());
    }
}