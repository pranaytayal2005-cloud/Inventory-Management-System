package com.inventory.controller;

import com.inventory.dto.HealthResponse;
import com.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing health check and status reporting for the API.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ProductService productService;

    public HealthController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Health check endpoint to verify system availability and current state.
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = new HealthResponse(
                "UP",
                "Inventory Management REST API",
                "1.0.0",
                productService.getTotalProductCount()
        );
        return ResponseEntity.ok(response);
    }
}
