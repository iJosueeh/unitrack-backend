package com.unitrack.backend.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.unitrack.backend.dashboard.entity.DashboardStats;
import com.unitrack.backend.dashboard.entity.ProjectStats;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;

@SpringBootTest
@ActiveProfiles("test")
class DashboardProjectStatsRepositoryTest {

    @Autowired
    private DashboardStatsRepository dashboardStatsRepository;

    @Autowired
    private ProjectStatsRepository projectStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Test
    void shouldPersistDashboardStatsWithWorkspaceRelation() {
        User owner = createUser("owner.dashboard@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace A");

        DashboardStats stats = new DashboardStats();
        stats.setWorkspace(workspace);
        stats.setProjectsCountActive(4);
        stats.setProjectsActiveDelta(1);
        stats.setTaskPendingCount(7);
        stats.setOverallProgressPercentage(new BigDecimal("64.50"));
        stats.setMembersCount(5);
        stats.setMembersOnsiteToday(3);

        DashboardStats saved = dashboardStatsRepository.saveAndFlush(stats);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWorkspace().getId()).isEqualTo(workspace.getId());
    }

    @Test
    void shouldEnforceUniqueWorkspaceInDashboardStats() {
        User owner = createUser("owner.unique.workspace@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace B");

        DashboardStats first = new DashboardStats();
        first.setWorkspace(workspace);
        dashboardStatsRepository.saveAndFlush(first);

        DashboardStats duplicate = new DashboardStats();
        duplicate.setWorkspace(workspace);

        assertThatThrownBy(() -> dashboardStatsRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistProjectStatsWithProjectRelation() {
        User owner = createUser("owner.project@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace C");
        Projects project = createProject(workspace, owner, "Project A");

        ProjectStats stats = new ProjectStats();
        stats.setProject(project);
        stats.setTasksCompletedThisWeek(12);
        stats.setHoursRegisteredThisWeek(28);
        stats.setTotalTasksCompleted(120);
        stats.setTotalHoursRegistered(new BigDecimal("340.25"));
        stats.setOverallProgressPercentage(new BigDecimal("72.10"));

        ProjectStats saved = projectStatsRepository.saveAndFlush(stats);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProject().getId()).isEqualTo(project.getId());
    }

    @Test
    void shouldEnforceUniqueProjectInProjectStats() {
        User owner = createUser("owner.unique.project@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace D");
        Projects project = createProject(workspace, owner, "Project B");

        ProjectStats first = new ProjectStats();
        first.setProject(project);
        projectStatsRepository.saveAndFlush(first);

        ProjectStats duplicate = new ProjectStats();
        duplicate.setProject(project);

        assertThatThrownBy(() -> projectStatsRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User createUser(String email) {
        User user = new User();
        user.setFirstName("Owner");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setRole(SystemRole.ADMIN);
        user.setIsActive(true);
        return userRepository.saveAndFlush(user);
    }

    private Workspaces createWorkspace(User owner, String name) {
        Workspaces workspace = new Workspaces();
        workspace.setName(name);
        workspace.setOwnerId(owner);
        return workspaceRepository.saveAndFlush(workspace);
    }

    private Projects createProject(Workspaces workspace, User owner, String name) {
        Projects project = new Projects();
        project.setName(name);
        project.setWorkspaces(workspace);
        project.setCreatedBy(owner);
        return projectsRepository.saveAndFlush(project);
    }
}
