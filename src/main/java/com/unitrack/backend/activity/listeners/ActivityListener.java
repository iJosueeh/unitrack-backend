package com.unitrack.backend.activity.listeners;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import com.unitrack.backend.activity.entity.Activity;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.activity.repository.ActivityRepository;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityListener {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @EventListener
    public void handle(ActivityEvent event) {
        Activity activity = new Activity();

        activity.setUser(userRepository.getReferenceById(event.getUserId()));
        activity.setAction(event.getAction());
        activity.setEntityType(event.getEntityType());
        activity.setEntityId(event.getEntityId());
        
        activityRepository.save(activity);
    }

}
