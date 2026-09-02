package com.zest.productapi.service;

import com.zest.productapi.dto.request.ItemRequest;
import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AsyncAuditService auditService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(page);

        PageResponse<ProductResponse> response = productService.getAllProducts(null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Product", response.getContent().get(0).getProductName());
    }

    @Test
    void getProductById_WhenFound_ShouldReturnProductResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getProductName());
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_ShouldSaveAndReturnResponse() {
        ProductRequest request = ProductRequest.builder()
                .productName("New Laptop")
                .items(List.of(ItemRequest.builder().quantity(5).build()))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request, "admin");

        assertNotNull(response);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(auditService, times(1)).logAudit(eq("CREATE_PRODUCT"), anyString(), eq("admin"));
    }

    @Test
    void deleteProduct_WhenFound_ShouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L, "admin");

        verify(productRepository, times(1)).delete(product);
        verify(auditService, times(1)).logAudit(eq("DELETE_PRODUCT"), anyString(), eq("admin"));
    }
}
