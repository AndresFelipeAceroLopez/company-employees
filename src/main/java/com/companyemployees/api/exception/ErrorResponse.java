package com.companyemployees.api.exception;

import java.util.Map;

public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        Map<String, String> errors
) {}
