package com.inventory.repository;

import com.inventory.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface defining operations for Product storage.
 */
public interface ProductRepository {

    /**
     * Saves a product. If id is null, generates a new ID.
     * If id exists, updates the product.
     */
    Product save(Product product);

    /**
     * Finds a product by its unique identifier.
     */
    Optional<Product> findById(Long id);

    /**
     * Retrieves all products.
     */
    List<Product> findAll();

    /**
     * Finds all products whose quantity is less than or equal to the specified threshold.
     */
    List<Product> findByQuantityLessThanEqual(int threshold);

    /**
     * Deletes a product by its identifier.
     * @return true if product was found and deleted, false otherwise
     */
    boolean deleteById(Long id);

    /**
     * Checks if a product exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Returns the total count of products in inventory.
     */
    int count();

    /**
     * Clears all products (primarily for testing purposes).
     */
    void clear();

    /**
     * Resets repository with default sample data.
     */
    void resetToSampleData();
}
