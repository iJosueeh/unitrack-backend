package com.unitrack.backend.tasks.repository;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.tasks.entity.Tasks;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindTaskByIdProjectAndWorkspace() {
        User owner = createUser("owner-task-find@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace Task Find");
        Projects project = createProject(workspace, owner, "Project Find");
        Tasks task = createTask(project, owner, "Task Find");

        Optional<Tasks> found = taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(
                task.getId(), project.getId(), workspace.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(task.getId());
    }

    @Test
    void shouldReturnEmptyWhenWorkspaceDoesNotMatch() {
        User owner = createUser("owner-task-mismatch@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace Match");
        Workspaces otherWorkspace = createWorkspace(owner, "Workspace Other");
        Projects project = createProject(workspace, owner, "Project Match");
        Tasks task = createTask(project, owner, "Task Match");

        Optional<Tasks> found = taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(
                task.getId(), project.getId(), otherWorkspace.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldListTasksByProjectAndWorkspace() {
        User owner = createUser("owner-task-list@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace List");
        Workspaces otherWorkspace = createWorkspace(owner, "Workspace Other List");

        Projects project = createProject(workspace, owner, "Project List");
        Projects otherProject = createProject(otherWorkspace, owner, "Project Other");

        Tasks first = createTask(project, owner, "Task A");
        Tasks second = createTask(project, owner, "Task B");
        createTask(otherProject, owner, "Task C");

        List<Tasks> result = taskRepository.findByProject_IdAndProject_Workspaces_IdOrderByCreatedAtDesc(
                project.getId(), workspace.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Tasks::getId)
                .contains(first.getId(), second.getId());
    }

    @Test
    void shouldCountTasksByProject() {
        User owner = createUser("owner-task-count@test.com");
        Workspaces workspace = createWorkspace(owner, "Workspace Count");
        Projects project = createProject(workspace, owner, "Project Count");

        createTask(project, owner, "Task 1");
        createTask(project, owner, "Task 2");

        long count = taskRepository.countByProject_Id(project.getId());

        assertThat(count).isEqualTo(2L);
    }

    private User createUser(String email) {
        User user = new User();
        user.setFirstName("Task");
        user.setLastName("Owner");
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
        workspace.setLimitMembers(10);
        return workspaceRepository.saveAndFlush(workspace);
    }

    private Projects createProject(Workspaces workspace, User owner, String name) {
        Projects project = new Projects();
        project.setName(name);
        project.setStatus(Status.TODO);
        project.setPriority(Priority.MEDIUM);
        project.setWorkspaces(workspace);
        project.setCreatedBy(owner);
        return projectsRepository.saveAndFlush(project);
    }

    private Tasks createTask(Projects project, User creator, String title) {
        Tasks task = new Tasks();
        task.setTitle(title);
        task.setDescription("desc");
        task.setStatus(Status.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setDueDate(Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")));
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setAssignedTo(creator);
        return taskRepository.saveAndFlush(task);
    }
}

