package com.unitrack.backend.activity.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.unitrack.backend.activity.entity.Activity;
import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.repository.ActivityRepository;
import com.unitrack.backend.projects.events.CreatedProjectEvent;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectsListener {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @EventListener
    public void handleProjectCreated(CreatedProjectEvent event) {
        Activity activity = new Activity();

        activity.setUser(userRepository.getReferenceById(event.getUserId()));
        activity.setAction(ActivityAction.CREATED);
        activity.setEntityType(ActivityEntityType.PROJECT);
        activity.setEntityId(event.getProjectId());
        activityRepository.save(activity);
    }

}
