package com.example.radnav;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")))
                .andExpect(jsonPath("$.service", is("radnav-web")));
    }

    @Test
    void taskListSupportsPaginationAndFiltering() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .param("limit", "100")
                        .param("offset", "0")
                        .param("state", "queued"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit", is(100)))
                .andExpect(jsonPath("$.offset", is(0)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void taskListRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndFetchTaskHappyPath() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "title", "Review ingestion pipeline",
                "description", "Validate the ingestion flow before release."));

        String created = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Review ingestion pipeline")))
                .andExpect(jsonPath("$.state", is("queued")))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/api/v1/tasks/{task_id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.title", is("Review ingestion pipeline")));
    }

    @Test
    void getTaskReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{task_id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nextBlockReturnsServerDrivenQuestionBlockAndPersistsBySession() throws Exception {
        String sessionId = "22222222-2222-2222-2222-222222222222";

        mockMvc.perform(get("/api/v1/sessions/{session_id}/next-block", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session_id", is(sessionId)))
                .andExpect(jsonPath("$.next_block.id", is("default")))
                .andExpect(jsonPath("$.next_block.role", is("default")))
                .andExpect(jsonPath("$.next_block.prompt", is("Continue?")));

        mockMvc.perform(get("/api/v1/sessions/{session_id}/next-block", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session_id", is(sessionId)))
                .andExpect(jsonPath("$.next_block.id", is("default")));
    }

    @Test
    void artifactDetailIncludesStatusConfidenceAndSource() throws Exception {
        mockMvc.perform(get("/api/v1/artifacts/{name}", "demo-artifact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("demo-artifact")))
                .andExpect(jsonPath("$.facts", hasSize(1)))
                .andExpect(jsonPath("$.facts[0].status", is("unknown")))
                .andExpect(jsonPath("$.facts[0].confidence", is(0.0)))
                .andExpect(jsonPath("$.facts[0].source", is("server")));
    }

    @Test
    void connectorIngestAcceptsVersionedSignedPayloadWithoutMessages() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "metadata", Map.of(
                        "version", "v1",
                        "site_id", "site-123",
                        "token", "site-123:signed-token"),
                "messages", java.util.List.of()));

        mockMvc.perform(post("/api/v1/connector/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted", is(true)))
                .andExpect(jsonPath("$.version", is("v1")))
                .andExpect(jsonPath("$.site_id", is("site-123")));
    }

    @Test
    void connectorIngestRejectsMessageBodiesAndBadSiteToken() throws Exception {
        String withMessages = objectMapper.writeValueAsString(Map.of(
                "metadata", Map.of(
                        "version", "v1",
                        "site_id", "site-123",
                        "token", "site-123:signed-token"),
                "messages", java.util.List.of(Map.of("body", "hello"))));

        mockMvc.perform(post("/api/v1/connector/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withMessages))
                .andExpect(status().isBadRequest());

        String badToken = objectMapper.writeValueAsString(Map.of(
                "metadata", Map.of(
                        "version", "v1",
                        "site_id", "site-123",
                        "token", "wrong-site:signed-token")));

        mockMvc.perform(post("/api/v1/connector/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badToken))
                .andExpect(status().isBadRequest());
    }
}
