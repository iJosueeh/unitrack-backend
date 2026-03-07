package com.unitrack.backend.activity.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.unitrack.backend.activity.entity.Activity;
import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.repository.ActivityRepository;
import com.unitrack.backend.auth.events.UserCreatedEvent;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsersListener {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        Activity activity = new Activity();

        activity.setUser(userRepository.getReferenceById(event.getUserId()));
        activity.setAction(ActivityAction.CREATED);
        activity.setEntityType(ActivityEntityType.USERS);
        activity.setEntityId(event.getUserId());

        activityRepository.save(activity);
    }

}
