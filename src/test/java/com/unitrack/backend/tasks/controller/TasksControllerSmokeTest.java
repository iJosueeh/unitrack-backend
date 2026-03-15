package com.unitrack.backend.tasks.controller;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.common.exception.GlobalExceptionHandler;
import com.unitrack.backend.tasks.dto.TaskResponse;
import com.unitrack.backend.tasks.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

@ExtendWith(MockitoExtension.class)
class TasksControllerSmokeTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TasksController(taskService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTask_WithValidBody_ShouldReturn201() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(taskService.createTask(eq(workspaceId), eq(projectId), any()))
                .thenReturn(taskResponse(taskId, projectId));

        String payload = """
                {
                  "title": "Task Alpha",
                  "description": "Desc",
                  "status": "TODO",
                  "priority": "HIGH",
                  "dueDate": 1773482400000
                }
                """;

        mockMvc.perform(post("/api/workspaces/{workspaceId}/projects/{projectId}/tasks", workspaceId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Task Alpha"));
    }

    @Test
    void createTask_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        String payload = """
                {
                  "description": "Missing title/status/priority"
                }
                """;

        mockMvc.perform(post("/api/workspaces/{workspaceId}/projects/{projectId}/tasks", workspaceId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getTasksByProject_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(taskService.getTasksByProject(workspaceId, projectId))
                .thenReturn(List.of(taskResponse(UUID.randomUUID(), projectId)));

        mockMvc.perform(get("/api/workspaces/{workspaceId}/projects/{projectId}/tasks", workspaceId, projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Task Alpha"));
    }

    @Test
    void getTaskById_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(taskService.getTaskById(workspaceId, projectId, taskId)).thenReturn(taskResponse(taskId, projectId));

        mockMvc.perform(get("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}", workspaceId, projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId.toString()));
    }

    @Test
    void updateTask_WithInvalidBody_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        String payload = """
                {
                  "title": "A"
                }
                """;

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}", workspaceId, projectId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void assignTask_WithValidBody_ShouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(taskService.assignTask(eq(workspaceId), eq(projectId), eq(taskId), any()))
                .thenReturn(taskResponse(taskId, projectId));

        String payload = """
                {
                  "assignedToId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/assign", workspaceId, projectId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateTaskStatus_WithMissingStatus_ShouldReturn400() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        String payload = """
                {
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(patch("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/status", workspaceId, projectId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteTask_WhenServiceThrowsAccessDenied_ShouldReturn403() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        doThrow(new AccessDeniedException("Only OWNER or ADMIN can manage this resource"))
                .when(taskService).deleteTask(workspaceId, projectId, taskId);

        mockMvc.perform(delete("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}", workspaceId, projectId, taskId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private TaskResponse taskResponse(UUID taskId, UUID projectId) {
        return new TaskResponse(
                taskId,
                "Task Alpha",
                "Desc",
                Status.TODO,
                Priority.HIGH,
                Timestamp.valueOf("2026-03-20 10:00:00"),
                projectId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

