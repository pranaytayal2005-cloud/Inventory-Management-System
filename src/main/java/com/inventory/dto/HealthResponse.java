package com.inventory.dto;

import java.time.LocalDateTime;

/**
 * Health response model for /api/health endpoint.
 */
public class HealthResponse {

    private String status;
    private String service;
    private LocalDateTime timestamp;
    private int totalProducts;
    private String version;

    public HealthResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public HealthResponse(String status, String service, String version, int totalProducts) {
        this.status = status;
        this.service = service;
        this.version = version;
        this.totalProducts = totalProducts;
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
