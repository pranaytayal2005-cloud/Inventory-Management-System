package com.inventory.service;

import com.inventory.dto.ProductRequest;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class encapsulating business logic for inventory management.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final int defaultLowStockThreshold;

    public ProductService(ProductRepository productRepository,
                          @Value("${inventory.default-low-stock-threshold:10}") int defaultLowStockThreshold) {
        this.productRepository = productRepository;
        this.defaultLowStockThreshold = defaultLowStockThreshold;
    }

    /**
     * Creates and stores a new product in the inventory.
     */
    public Product createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        Product product = new Product(
                null,
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getPrice(),
                request.getQuantity(),
                request.getCategory().trim()
        );
        Product saved = productRepository.save(product);
        log.info("Product created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieves a single product by its ID. Throws ResourceNotFoundException if absent.
     */
    public Product getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    /**
     * Retrieves all products currently stored in inventory.
     */
    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Updates an existing product with new details. Throws ResourceNotFoundException if absent.
     */
    public Product updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        existing.setPrice(request.getPrice());
        existing.setQuantity(request.getQuantity());
        existing.setCategory(request.getCategory().trim());

        Product updated = productRepository.save(existing);
        log.info("Product with ID: {} updated successfully", id);
        return updated;
    }

    /**
     * Deletes a product by its ID. Throws ResourceNotFoundException if absent.
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        boolean deleted = productRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        log.info("Product with ID: {} deleted successfully", id);
    }

    /**
     * Returns products having quantity less than or equal to the specified threshold.
     * If threshold is null, defaults to application-configured threshold.
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        int effectiveThreshold = (threshold != null) ? threshold : defaultLowStockThreshold;
        if (effectiveThreshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative: " + effectiveThreshold);
        }
        log.debug("Fetching low-stock products with threshold: {}", effectiveThreshold);
        return productRepository.findByQuantityLessThanEqual(effectiveThreshold);
    }

    /**
     * Returns total count of products in the inventory.
     */
    public int getTotalProductCount() {
        return productRepository.count();
    }

    /**
     * Resets repository back to sample data.
     */
    public void resetToSampleData() {
        log.info("Resetting inventory repository to sample data");
        productRepository.resetToSampleData();
    }
}
