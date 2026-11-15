package com.inventory.repository;

import com.inventory.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of ProductRepository.
 * Uses ConcurrentHashMap for concurrent safety and AtomicLong for auto-increment IDs.
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        resetToSampleData();
    }

    @Override
    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();

        if (product.getId() == null) {
            Long newId = idSequence.incrementAndGet();
            product.setId(newId);
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
        } else {
            Product existing = productStore.get(product.getId());
            if (existing != null && product.getCreatedAt() == null) {
                product.setCreatedAt(existing.getCreatedAt());
            } else if (product.getCreatedAt() == null) {
                product.setCreatedAt(now);
            }
            product.setUpdatedAt(now);
        }

        productStore.put(product.getId(), cloneProduct(product));
        return cloneProduct(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Product product = productStore.get(id);
        return Optional.ofNullable(product != null ? cloneProduct(product) : null);
    }

    @Override
    public List<Product> findAll() {
        return productStore.values().stream()
                .sorted(Comparator.comparing(Product::getId))
                .map(this::cloneProduct)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByQuantityLessThanEqual(int threshold) {
        return productStore.values().stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() <= threshold)
                .sorted(Comparator.comparing(Product::getQuantity).thenComparing(Product::getId))
                .map(this::cloneProduct)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return productStore.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && productStore.containsKey(id);
    }

    @Override
    public int count() {
        return productStore.size();
    }

    @Override
    public void clear() {
        productStore.clear();
        idSequence.set(0);
    }

    @Override
    public void resetToSampleData() {
        clear();
        List<Product> sampleProducts = List.of(
                new Product(null, "Standard Business Laptop", "15.6 inch Quad-Core, 16GB RAM, 512GB SSD", new BigDecimal("899.99"), 15, "Electronics"),
                new Product(null, "Wireless Optical Mouse", "Ergonomic 2.4GHz wireless optical mouse", new BigDecimal("24.99"), 8, "Electronics"),
                new Product(null, "Mechanical Gaming Keyboard", "Compact mechanical keyboard with backlight", new BigDecimal("69.99"), 4, "Electronics"),
                new Product(null, "Ultra HD Monitor 27-inch", "IPS panel with HDMI and DisplayPort input", new BigDecimal("279.50"), 5, "Electronics"),
                new Product(null, "Multiport USB-C Adapter", "Multi-port adapter with HDMI and USB 3.0", new BigDecimal("29.99"), 25, "Accessories"),
                new Product(null, "Ergonomic Desk Chair", "Adjustable swivel chair with lumbar support", new BigDecimal("189.00"), 3, "Furniture"),
                new Product(null, "Height-Adjustable Standing Desk", "Dual-motor motorized desk frame with bamboo top", new BigDecimal("450.00"), 12, "Furniture"),
                new Product(null, "Over-Ear Wireless Headphones", "Rechargeable noise isolation headphones", new BigDecimal("129.00"), 18, "Audio")
        );

        for (Product product : sampleProducts) {
            save(product);
        }
    }

    private Product cloneProduct(Product original) {
        Product copy = new Product();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setDescription(original.getDescription());
        copy.setPrice(original.getPrice());
        copy.setQuantity(original.getQuantity());
        copy.setCategory(original.getCategory());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        return copy;
    }
}
