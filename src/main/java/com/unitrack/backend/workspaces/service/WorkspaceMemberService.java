package com.unitrack.backend.workspaces.service;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspacesMembersRepository workspacesMembersRepository;

    public void createOwnerMembership(Workspaces workspace, User owner) {
        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setUser(owner);
        membership.setRole(WorkspaceRole.OWNER);
        membership.setJoinedAt(new Timestamp(System.currentTimeMillis()));
        workspacesMembersRepository.save(membership);
    }

}
