package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class MockDraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String draftBody(String userId, String applicationId, double totalAmount) {
        return """
                {
                  "sourceSystem": "laa-civil-manage",
                  "draftType": "PRIOR_AUTHORITY",
                  "applicationId": "%s",
                  "userId": "%s",
                  "draftBody": {
                    "expertName": "Dr Joe Bloggs",
                    "totalAmount": %s
                  }
                }
                """.formatted(applicationId, userId, totalAmount);
    }

    @Test
    void createReturns201WithDraftId() throws Exception {
        mockMvc.perform(post("/mock-access-data-store/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody("user-1", UUID.randomUUID().toString(), 135.00)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.draftId").exists());
    }

    @Test
    void updateAfterCreateReturns200AndReplacesBody() throws Exception {
        String applicationId = UUID.randomUUID().toString();
        String userId = "user-update-" + UUID.randomUUID();

        MvcResult created = mockMvc.perform(post("/mock-access-data-store/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(userId, applicationId, 135.00)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
        String draftId = createdBody.get("draftId").asText();

        mockMvc.perform(put("/mock-access-data-store/drafts/{draftId}", draftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(userId, applicationId, 180.00)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/mock-access-data-store/drafts")
                        .param("sourceSystem", "laa-civil-manage")
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].draftId").value(draftId))
                .andExpect(jsonPath("$[0].draftBody.totalAmount").value(180.00));
    }

    @Test
    void updateReturns404WhenDraftDoesNotExist() throws Exception {
        mockMvc.perform(put("/mock-access-data-store/drafts/{draftId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody("user-x", UUID.randomUUID().toString(), 99.00)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFiltersByUserAndDraftType() throws Exception {
        String userId = "user-filter-" + UUID.randomUUID();
        String otherUserId = "user-other-" + UUID.randomUUID();
        String applicationId = UUID.randomUUID().toString();

        mockMvc.perform(post("/mock-access-data-store/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(userId, applicationId, 100.00)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/mock-access-data-store/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(otherUserId, applicationId, 200.00)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mock-access-data-store/drafts")
                        .param("sourceSystem", "laa-civil-manage")
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].draftBody.totalAmount").value(100.00));
    }

    @Test
    void deleteReturns204AndRemovesDraft() throws Exception {
        String userId = "user-delete-" + UUID.randomUUID();
        String applicationId = UUID.randomUUID().toString();

        MvcResult created = mockMvc.perform(post("/mock-access-data-store/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(userId, applicationId, 50.00)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
        String draftId = createdBody.get("draftId").asText();

        mockMvc.perform(delete("/mock-access-data-store/drafts/{draftId}", draftId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mock-access-data-store/drafts")
                        .param("sourceSystem", "laa-civil-manage")
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
