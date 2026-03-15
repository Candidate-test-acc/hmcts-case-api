package uk.gov.hmcts.dts_dev_challenge.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dts_dev_challenge.repository.CaseRepository;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CaseControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    CaseRepository repo;

    @BeforeEach
    void cleanUp() {
        repo.deleteAll();
    }

    @Test
    void post_creates_case_and_persists_it() throws Exception {
        mvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Integration test case 2",
                              "description": "Created through API"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reference", startsWith("HMCTS-")))
                .andExpect(jsonPath("$.title").value("Integration test case 2"))
                .andExpect(jsonPath("$.description").value("Created through API"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.outcome").value("NONE"));
    }
    @Test
    void create_then_get_case_by_id() throws Exception {
        String response = mvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Get test case",
                          "description": "Fetch through API"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long caseId = id.longValue();

        mvc.perform(get("/api/cases/" + caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caseId))
                .andExpect(jsonPath("$.title").value("Get test case"))
                .andExpect(jsonPath("$.description").value("Fetch through API"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.outcome").value("NONE"));

    }

    @Test
    void patch_rejected_outcome_auto_closes_case() throws Exception {
        String response = mvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Patch test case",
                          "description": "Patch through API"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long caseId = id.longValue();

        mvc.perform(patch("/api/cases/" + caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "outcome": "REJECTED",
                          "changeReason": "Rejected by tribunal"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caseId))
                .andExpect(jsonPath("$.outcome").value("REJECTED"))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
    @Test
    void soft_delete_hides_case_from_normal_get() throws Exception {
        String response = mvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Delete test case",
                          "description": "Soft delete through API"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long caseId = id.longValue();

        mvc.perform(delete("/api/cases/" + caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "reason": "Duplicate"
                    }
                    """))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cases/" + caseId))
                .andExpect(status().isNotFound());
    }
    @Test
    void get_unknown_case_returns_404() throws Exception {
        mvc.perform(get("/api/cases/999999"))
                .andExpect(status().isNotFound());
    }
}