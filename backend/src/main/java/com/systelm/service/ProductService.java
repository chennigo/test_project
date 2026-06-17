package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateCategoryCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.UpdateProductCommand;
import com.systelm.entity.Product;
import com.systelm.entity.ProductBatch;
import com.systelm.entity.ProductCategory;
import com.systelm.repository.ProductBatchRepository;
import com.systelm.repository.ProductCategoryRepository;
import com.systelm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductBatchRepository batchRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductCategoryRepository categoryRepository,
            ProductBatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.batchRepository = batchRepository;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product createProduct(CreateProductCommand cmd) {
        Product product = new Product();
        product.setName(cmd.name());
        product.setSku(cmd.sku());
        product.setCostPrice(cmd.costPrice());
        product.setSalePrice(cmd.salePrice());
        product.setUnit(cmd.unit());
        product.setMinStock(cmd.minStock());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductCommand cmd) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (cmd.name() != null) {
            product.setName(cmd.name());
        }
        if (cmd.sku() != null) {
            product.setSku(cmd.sku());
        }
        if (cmd.categoryId() != null) {
            product.setCategoryId(cmd.categoryId());
        }
        if (cmd.costPrice() != null) {
            product.setCostPrice(cmd.costPrice());
        }
        if (cmd.salePrice() != null) {
            product.setSalePrice(cmd.salePrice());
        }
        if (cmd.unit() != null) {
            product.setUnit(cmd.unit());
        }
        if (cmd.minStock() != null) {
            product.setMinStock(cmd.minStock());
        }
        if (cmd.status() != null) {
            product.setStatus(cmd.status());
        }
        return productRepository.save(product);
    }

    public List<ProductCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public ProductCategory createCategory(CreateCategoryCommand cmd) {
        ProductCategory category = new ProductCategory();
        category.setName(cmd.name());
        category.setParentId(cmd.parentId());
        return categoryRepository.save(category);
    }

    public List<ProductBatch> listBatches(Long productId) {
        return batchRepository.findByProductId(productId);
    }

    @Transactional
    public ProductBatch createBatch(CreateBatchCommand cmd) {
        productRepository.findById(cmd.productId())
            .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        ProductBatch batch = new ProductBatch();
        batch.setProductId(cmd.productId());
        batch.setBatchNo(cmd.batchNo());
        batch.setProductionDate(cmd.productionDate());
        return batchRepository.save(batch);
    }
}
