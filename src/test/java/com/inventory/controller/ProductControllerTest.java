package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.ProductRequest;
import com.inventory.exception.GlobalExceptionHandler;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Product;
import com.inventory.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products - Should return list of products with 200 OK")
    void testGetAllProducts() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Laptop", "Gaming Laptop", new BigDecimal("1200.00"), 10, "Electronics"),
                new Product(2L, "Mouse", "Optical Mouse", new BigDecimal("25.00"), 50, "Electronics")
        );
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Laptop")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Mouse")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product when ID exists")
    void testGetProductById_Found() throws Exception {
        Product product = new Product(1L, "Desk", "Standing Desk", new BigDecimal("350.00"), 8, "Furniture");
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Desk")))
                .andExpect(jsonPath("$.price", is(350.00)))
                .andExpect(jsonPath("$.quantity", is(8)));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 404 Not Found when product does not exist")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product", "id", 999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: '999'")));
    }

    @Test
    @DisplayName("GET /api/products/low-stock - Should return low stock products")
    void testGetLowStockProducts() throws Exception {
        List<Product> lowStock = List.of(
                new Product(3L, "Keyboard", "Mechanical", new BigDecimal("75.00"), 2, "Electronics")
        );
        when(productService.getLowStockProducts(5)).thenReturn(lowStock);

        mockMvc.perform(get("/api/products/low-stock").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Keyboard")))
                .andExpect(jsonPath("$[0].quantity", is(2)));
    }

    @Test
    @DisplayName("POST /api/products - Should create product and return 201 Created with Location header")
    void testAddProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest("Webcam", "1080p HD Webcam", new BigDecimal("49.99"), 20, "Electronics");
        Product createdProduct = new Product(10L, "Webcam", "1080p HD Webcam", new BigDecimal("49.99"), 20, "Electronics");

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/10"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Webcam")))
                .andExpect(jsonPath("$.price", is(49.99)))
                .andExpect(jsonPath("$.quantity", is(20)));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 Bad Request when validation fails")
    void testAddProduct_ValidationFailure() throws Exception {
        // Missing name, negative price, negative quantity, blank category
        ProductRequest invalidRequest = new ProductRequest("", "Some desc", new BigDecimal("-10.00"), -5, "");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.quantity").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update product and return 200 OK")
    void testUpdateProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest("Updated Chair", "Ergonomic mesh", new BigDecimal("180.00"), 15, "Furniture");
        Product updatedProduct = new Product(5L, "Updated Chair", "Ergonomic mesh", new BigDecimal("180.00"), 15, "Furniture");

        when(productService.updateProduct(eq(5L), any(ProductRequest.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.name", is("Updated Chair")))
                .andExpect(jsonPath("$.price", is(180.00)));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should return 404 when product to update does not exist")
    void testUpdateProduct_NotFound() throws Exception {
        ProductRequest request = new ProductRequest("Non Existent", "Desc", new BigDecimal("10.00"), 5, "General");
        when(productService.updateProduct(eq(404L), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product", "id", 404L));

        mockMvc.perform(put("/api/products/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should return 204 No Content when product deleted")
    void testDeleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should return 404 Not Found when product does not exist")
    void testDeleteProduct_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Product", "id", 999L))
                .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
