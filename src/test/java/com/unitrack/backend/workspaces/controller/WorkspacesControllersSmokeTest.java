package com.unitrack.backend.workspaces.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.unitrack.backend.common.exception.GlobalExceptionHandler;
import com.unitrack.backend.workspaces.dto.WorkspaceResponse;
import com.unitrack.backend.workspaces.service.WorkspaceInviteService;
import com.unitrack.backend.workspaces.service.WorkspaceMemberService;
import com.unitrack.backend.workspaces.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class WorkspacesControllersSmokeTest {

    private MockMvc mockMvc;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private WorkspaceMemberService workspaceMemberService;

    @Mock
    private WorkspaceInviteService workspaceInviteService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new WorkspacesController(workspaceService),
                new WorkspacesMembersController(workspaceMemberService),
                new WorkspacesInviteController(workspaceInviteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyWorkspaces_ShouldReturn200() throws Exception {
        WorkspaceResponse response = WorkspaceResponse.builder()
                .id(UUID.randomUUID())
                .name("Acme")
                .ownerId(UUID.randomUUID())
                .membersCount(2)
                .projectsCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(workspaceService.getMyWorkspaces()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createWorkspace_WithInvalidBody_ShouldReturn400() throws Exception {
        String invalidPayload = """
                {
                  "name": "",
                  "limitMembers": 0
                }
                """;

        mockMvc.perform(post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMemberRole_WithValidBody_ShouldReturn200() throws Exception {
        String payload = """
                {
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(
                patch("/api/workspaces/{workspaceId}/members/{userId}/role", UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void acceptInvite_WithBlankCode_ShouldReturn400() throws Exception {
        String payload = """
                {
                  "code": ""
                }
                """;

        mockMvc.perform(post("/api/workspaces/invites/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateInvite_WhenServiceThrowsIllegalArgument_ShouldReturn400() throws Exception {
        doThrow(new IllegalArgumentException("Invite not found"))
                .when(workspaceInviteService)
                .deactivateInvite(any(UUID.class));

        mockMvc.perform(delete("/api/workspaces/invites/{inviteId}", UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getActiveInvite_WhenServiceThrowsAccessDenied_ShouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(workspaceInviteService)
                .getActiveInvite(any(UUID.class));

        mockMvc.perform(get("/api/workspaces/invites/workspace/{workspaceId}/active", UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
