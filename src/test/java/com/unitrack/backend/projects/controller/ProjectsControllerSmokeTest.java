package com.unitrack.backend.projects.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.common.exception.GlobalExceptionHandler;
import com.unitrack.backend.projects.dto.ProjectResponse;
import com.unitrack.backend.projects.dto.ProjectSummaryResponse;
import com.unitrack.backend.projects.service.ProjectService;

@ExtendWith(MockitoExtension.class)
class ProjectsControllerSmokeTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectsController(projectService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createProject_WithValidBody_ShouldReturn201() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(projectService.createProject(eq(workspaceId), any())).thenReturn(projectResponse(projectId, workspaceId));

        String payload = """
                {
                  "name": "Project Alpha",
                  "description": "Desc",
                  "client": "Acme",
                  "status": "TODO",
                  "priority": "HIGH",
                  "budget": 1500.00,
                  "startDate": 1741946400000,
                  "endDate": 1742464800000
                }
                """;

        mockMvc.perform(post("/api/workspaces/{workspaceId}/projects", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Project Alpha"));
    }

    @Test
    void createProject_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        String payload = """
                {
                  "description": "No name, no status"
                }
                """;

        mockMvc.perform(post("/api/workspaces/{workspaceId}/projects", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getProjectsByWorkspace_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        ProjectSummaryResponse summary = new ProjectSummaryResponse(
                UUID.randomUUID(), "Project Alpha", "Acme",
                Status.TODO, Priority.MEDIUM,
                Timestamp.valueOf("2026-03-14 10:00:00"),
                Timestamp.valueOf("2026-03-20 10:00:00"),
                UUID.randomUUID(), "John Doe",
                UUID.randomUUID(), "Jane Roe",
                3L, LocalDateTime.now(), LocalDateTime.now());

        when(projectService.getProjectsByWorkspace(workspaceId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/workspaces/{workspaceId}/projects", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Project Alpha"));
    }

    @Test
    void getProjectById_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(projectService.getProjectById(workspaceId, projectId)).thenReturn(projectResponse(projectId, workspaceId));

        mockMvc.perform(get("/api/workspaces/{workspaceId}/projects/{projectId}", workspaceId, projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(projectId.toString()));
    }

    @Test
    void updateProject_WithInvalidBody_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        String payload = """
                {
                  "name": "A"
                }
                """;

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}", workspaceId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void assignProjectMember_WithValidBody_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(projectService.assignProjectMember(eq(workspaceId), eq(projectId), any()))
                .thenReturn(projectResponse(projectId, workspaceId));

        String payload = """
                {
                  "assignedToId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}/assign", workspaceId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unassignProjectMember_WhenServiceThrowsIllegalArgument_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Project not found"))
                .when(projectService).unassignProjectMember(workspaceId, projectId);

        mockMvc.perform(delete("/api/workspaces/{workspaceId}/projects/{projectId}/assign", workspaceId, projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProjectStatus_WithMissingStatus_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        String payload = """
                {
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}/status", workspaceId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private ProjectResponse projectResponse(UUID projectId, UUID workspaceId) {
        return new ProjectResponse(
                projectId, "Project Alpha", "Desc", "Acme",
                Status.TODO, Priority.HIGH, BigDecimal.valueOf(1500),
                Timestamp.valueOf("2026-03-14 10:00:00"),
                Timestamp.valueOf("2026-03-20 10:00:00"),
                UUID.randomUUID(), UUID.randomUUID(), workspaceId,
                LocalDateTime.now(), LocalDateTime.now());
    }
}

