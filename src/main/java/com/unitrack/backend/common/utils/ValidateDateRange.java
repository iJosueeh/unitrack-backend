package com.unitrack.backend.common.utils;

import java.sql.Timestamp;

public class ValidateDateRange {

    public void validateDateRange(Timestamp startDate, Timestamp endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Project start date and end date are required");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Project start date cannot be after end date");
        }
    }
}