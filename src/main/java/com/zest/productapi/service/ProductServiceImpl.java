package com.zest.productapi.service;

import com.zest.productapi.dto.request.ItemRequest;
import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.ItemResponse;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.entity.Item;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.BadRequestException;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final AsyncAuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(String search, Pageable pageable) {
        Page<Product> productPage;
        if (search != null && !search.trim().isEmpty()) {
            productPage = productRepository.findByProductNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        Page<ProductResponse> responsePage = productPage.map(this::mapToProductResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductEntity(id);
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String currentUser) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .createdBy(currentUser)
                .createdOn(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ItemRequest itemReq : request.getItems()) {
                Item item = Item.builder()
                        .product(product)
                        .quantity(itemReq.getQuantity())
                        .build();
                product.getItems().add(item);
            }
        }

        Product savedProduct = productRepository.save(product);
        auditService.logAudit("CREATE_PRODUCT", "Created product with ID: " + savedProduct.getId(), currentUser);
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String currentUser) {
        Product product = findProductEntity(id);
        product.setProductName(request.getProductName());
        product.setModifiedBy(currentUser);
        product.setModifiedOn(LocalDateTime.now());

        if (request.getItems() != null) {
            product.getItems().clear();
            for (ItemRequest itemReq : request.getItems()) {
                Item item = Item.builder()
                        .product(product)
                        .quantity(itemReq.getQuantity())
                        .build();
                product.getItems().add(item);
            }
        }

        Product updatedProduct = productRepository.save(product);
        auditService.logAudit("UPDATE_PRODUCT", "Updated product with ID: " + updatedProduct.getId(), currentUser);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String currentUser) {
        Product product = findProductEntity(id);
        productRepository.delete(product);
        auditService.logAudit("DELETE_PRODUCT", "Deleted product with ID: " + id, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getProductItems(Long productId) {
        findProductEntity(productId);
        List<Item> items = itemRepository.findByProductId(productId);
        return items.stream().map(this::mapToItemResponse).toList();
    }

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Long productId, ItemRequest request, String currentUser) {
        Product product = findProductEntity(productId);
        Item item = Item.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();
        Item savedItem = itemRepository.save(item);
        auditService.logAudit("ADD_ITEM", "Added item ID " + savedItem.getId() + " to product ID " + productId, currentUser);
        return mapToItemResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteItemFromProduct(Long productId, Long itemId, String currentUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
        if (!item.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Item does not belong to product with id: " + productId);
        }
        itemRepository.delete(item);
        auditService.logAudit("DELETE_ITEM", "Deleted item ID " + itemId + " from product ID " + productId, currentUser);
    }

    private Product findProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse mapToProductResponse(Product product) {
        List<ItemResponse> itemResponses = product.getItems() != null ?
                product.getItems().stream().map(this::mapToItemResponse).toList() : new ArrayList<>();

        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .items(itemResponses)
                .build();
    }

    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }
}
