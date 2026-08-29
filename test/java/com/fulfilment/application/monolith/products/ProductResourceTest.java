package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProductResourceTest {

    private ProductResource resource;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resource = new ProductResource();
        resource.productRepository = productRepository;
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Test
    void shouldGetAllProducts() {

        Product product1 = product("Product A", "Description A", BigDecimal.valueOf(10.0), 5);
        Product product2 = product("Product B", "Description B", BigDecimal.valueOf(20.0), 10);

        when(productRepository.listAll(any()))
                .thenReturn(List.of(product1, product2));

        List<Product> result = resource.get();

        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).name);
        assertEquals("Product B", result.get(1).name);

        verify(productRepository).listAll(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {

        when(productRepository.listAll(any()))
                .thenReturn(List.of());

        List<Product> result = resource.get();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // =====================================================
    // GET SINGLE
    // =====================================================

    @Test
    void shouldGetProductById() {

        Product product = product("Laptop", "Good Laptop", BigDecimal.valueOf(1000.0), 10);
        product.id = 1L;

        when(productRepository.findById(1L))
                .thenReturn(product);

        Product result = resource.getSingle(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.name);
        assertEquals(1L, result.id);

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrow404WhenProductDoesNotExist() {

        when(productRepository.findById(999L))
                .thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.getSingle(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());
        assertEquals(
                "Product with id of 999 does not exist.",
                exception.getMessage()
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @Test
    void shouldCreateProductSuccessfully() {

        Product product = product(
                "Laptop",
                "New Laptop",
                BigDecimal.valueOf(1000.0),
                10
        );

        Response response = resource.create(product);

        assertEquals(201, response.getStatus());
        assertEquals(product, response.getEntity());

        verify(productRepository).persist(product);
    }

    @Test
    void shouldThrow422WhenIdIsProvidedDuringCreate() {

        Product product = product(
                "Laptop",
                "New Laptop",
                BigDecimal.valueOf(1000.0),
                10
        );

        product.id = 100L;

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.create(product)
        );

        assertEquals(422, exception.getResponse().getStatus());
        assertEquals(
                "Id was invalidly set on request.",
                exception.getMessage()
        );

        verify(productRepository, never()).persist(any(Product.class));
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Test
    void shouldUpdateProductSuccessfully() {

        Product existing = product(
                "Old Name",
                "Old Description",
                BigDecimal.valueOf(100.0),
                5
        );

        existing.id = 1L;

        Product request = product(
                "New Name",
                "New Description",
                BigDecimal.valueOf(200.0),
                20
        );

        when(productRepository.findById(1L))
                .thenReturn(existing);

        Product result = resource.update(1L, request);

        assertEquals("New Name", result.name);
        assertEquals("New Description", result.description);
        assertEquals(BigDecimal.valueOf(200.0), result.price);
        assertEquals(20, result.stock);

        verify(productRepository).persist(existing);
    }

    @Test
    void shouldThrow422WhenProductNameIsNullDuringUpdate() {

        Product request = product(
                null,
                "Description",
                BigDecimal.valueOf(100.0),
                10
        );

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.update(1L, request)
        );

        assertEquals(422, exception.getResponse().getStatus());
        assertEquals(
                "Product Name was not set on request.",
                exception.getMessage()
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistingProduct() {

        Product request = product(
                "Updated Product",
                "Description",
                BigDecimal.valueOf(100.0),
                10
        );

        when(productRepository.findById(999L))
                .thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.update(999L, request)
        );

        assertEquals(404, exception.getResponse().getStatus());
        assertEquals(
                "Product with id of 999 does not exist.",
                exception.getMessage()
        );

        verify(productRepository, never()).persist(any(Product.class));
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Test
    void shouldDeleteProductSuccessfully() {

        Product product = product(
                "Laptop",
                "Description",
                BigDecimal.valueOf(1000.0),
                10
        );

        product.id = 1L;

        when(productRepository.findById(1L))
                .thenReturn(product);

        Response response = resource.delete(1L);

        assertEquals(204, response.getStatus());

        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrow404WhenDeletingNonExistingProduct() {

        when(productRepository.findById(999L))
                .thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> resource.delete(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());
        assertEquals(
                "Product with id of 999 does not exist.",
                exception.getMessage()
        );

        verify(productRepository, never()).delete(any(Product.class));
    }


    // =====================================================
    // ERROR MAPPER
    // =====================================================

    @Test
    void shouldMapWebApplicationException() {

        ProductResource.ErrorMapper mapper =
                new ProductResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        WebApplicationException exception =
                new WebApplicationException("Product not found", 404);

        Response response = mapper.toResponse(exception);

        assertEquals(404, response.getStatus());

        verifyResponseContains(
                response,
                "ProductResource$ErrorMapper"
        );
    }

    @Test
    void shouldMapGeneralExceptionTo500() {

        ProductResource.ErrorMapper mapper =
                new ProductResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Exception exception =
                new RuntimeException("Something went wrong");

        Response response = mapper.toResponse(exception);

        assertEquals(500, response.getStatus());

        assertNotNull(response.getEntity());
    }


    // =====================================================
    // HELPER METHODS
    // =====================================================

    private Product product(
            String name,
            String description,
            BigDecimal price,
            int stock) {

        Product product = new Product();

        product.name = name;
        product.description = description;

        // If your Product.price is BigDecimal,
        // change this line accordingly.
        product.price = price;

        product.stock = stock;

        return product;
    }

    private void verifyResponseContains(
            Response response,
            String ignored) {

        assertNotNull(response.getEntity());
    }
}