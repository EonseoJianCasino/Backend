package com.test.webtest.domain.securityvitals.service;

import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;

import java.util.UUID;

public interface SecurityVitalsService {
    void scanAndSave(UUID testId);
    SecurityVitalsView getView(UUID testId);
}
