package com.inventory.service;

import com.inventory.dto.ProductRequest;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, 10);
    }

    @Test
    @DisplayName("Should successfully create a new product")
    void testCreateProduct_Success() {
        ProductRequest request = new ProductRequest("Wireless Mouse", "Ergonomic 2.4GHz", new BigDecimal("29.99"), 50, "Electronics");
        Product savedProduct = new Product(1L, "Wireless Mouse", "Ergonomic 2.4GHz", new BigDecimal("29.99"), 50, "Electronics");

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Wireless Mouse");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(result.getQuantity()).isEqualTo(50);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should find product by ID when product exists")
    void testGetProductById_Found() {
        Product product = new Product(1L, "Monitor", "4K HDR", new BigDecimal("399.99"), 15, "Electronics");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
    void testGetProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: '99'");
    }

    @Test
    @DisplayName("Should retrieve all products")
    void testGetAllProducts() {
        List<Product> mockList = List.of(
                new Product(1L, "Mouse", "Optic", new BigDecimal("19.99"), 20, "Electronics"),
                new Product(2L, "Keyboard", "Mechanical", new BigDecimal("79.99"), 10, "Electronics")
        );
        when(productRepository.findAll()).thenReturn(mockList);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Mouse");
        assertThat(result.get(1).getName()).isEqualTo("Keyboard");
    }

    @Test
    @DisplayName("Should update existing product successfully")
    void testUpdateProduct_Success() {
        Product existingProduct = new Product(1L, "Old Mouse", "Old Desc", new BigDecimal("15.00"), 5, "Electronics");
        ProductRequest updateRequest = new ProductRequest("New Mouse", "New Desc", new BigDecimal("25.00"), 12, "Peripherals");
        Product updatedProduct = new Product(1L, "New Mouse", "New Desc", new BigDecimal("25.00"), 12, "Peripherals");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Mouse");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("25.00"));
        assertThat(result.getQuantity()).isEqualTo(12);
        assertThat(result.getCategory()).isEqualTo("Peripherals");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void testUpdateProduct_NotFound_ThrowsException() {
        ProductRequest updateRequest = new ProductRequest("Item", "Desc", new BigDecimal("10.00"), 5, "General");
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully when it exists")
    void testDeleteProduct_Success() {
        when(productRepository.deleteById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void testDeleteProduct_NotFound_ThrowsException() {
        when(productRepository.deleteById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should return low-stock products using default threshold")
    void testGetLowStockProducts_DefaultThreshold() {
        List<Product> lowStock = List.of(
                new Product(1L, "Mouse", "Desc", new BigDecimal("20.00"), 4, "Electronics")
        );
        when(productRepository.findByQuantityLessThanEqual(10)).thenReturn(lowStock);

        List<Product> result = productService.getLowStockProducts(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isLessThanOrEqualTo(10);
        verify(productRepository).findByQuantityLessThanEqual(10);
    }

    @Test
    @DisplayName("Should return low-stock products using custom threshold")
    void testGetLowStockProducts_CustomThreshold() {
        List<Product> lowStock = List.of(
                new Product(1L, "Mouse", "Desc", new BigDecimal("20.00"), 3, "Electronics")
        );
        when(productRepository.findByQuantityLessThanEqual(5)).thenReturn(lowStock);

        List<Product> result = productService.getLowStockProducts(5);

        assertThat(result).hasSize(1);
        verify(productRepository).findByQuantityLessThanEqual(5);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when low stock threshold is negative")
    void testGetLowStockProducts_NegativeThreshold_ThrowsException() {
        assertThatThrownBy(() -> productService.getLowStockProducts(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Should return total product count")
    void testGetTotalProductCount() {
        when(productRepository.count()).thenReturn(8);

        int result = productService.getTotalProductCount();

        assertThat(result).isEqualTo(8);
        verify(productRepository).count();
    }
}
