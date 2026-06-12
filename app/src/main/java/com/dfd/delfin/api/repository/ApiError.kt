package com.dfd.delfin.api.repository

/**
 * Categorized API error types for consistent error handling across the application.
 * Each error type represents a specific failure scenario that can occur during API calls.
 */
enum class ApiError {
    /**
     * Request timed out - occurs when SocketTimeoutException is thrown.
     * Indicates the server took too long to respond.
     */
    Timeout,

    /**
     * Network connectivity issues - occurs when IOException is thrown.
     * Indicates problems with internet connection or network availability.
     */
    Network,

    /**
     * HTTP 401 - Authentication required.
     * Indicates the user needs to log in or refresh their authentication token.
     */
    Unauthorized,

    /**
     * HTTP 403 - Insufficient permissions.
     * Indicates the user doesn't have access to the requested resource.
     */
    AccessDenied,

    /**
     * HTTP 404 - Resource not found.
     * Indicates the requested endpoint or resource doesn't exist.
     */
    NotFound,

    /**
     * HTTP 503 - Service temporarily unavailable.
     * Indicates the server is temporarily unable to handle the request.
     */
    ServiceUnavailable,

    /**
     * All other unhandled errors.
     * Catch-all for unexpected exceptions or unrecognized HTTP status codes.
     */
    Unknown
}
