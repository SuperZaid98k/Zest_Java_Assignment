package com.zest.productapi.service;

import com.zest.productapi.dto.request.ItemRequest;
import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.ItemResponse;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    PageResponse<ProductResponse> getAllProducts(String search, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request, String currentUser);
    ProductResponse updateProduct(Long id, ProductRequest request, String currentUser);
    void deleteProduct(Long id, String currentUser);
    List<ItemResponse> getProductItems(Long productId);
    ItemResponse addItemToProduct(Long productId, ItemRequest request, String currentUser);
    void deleteItemFromProduct(Long productId, Long itemId, String currentUser);
}
