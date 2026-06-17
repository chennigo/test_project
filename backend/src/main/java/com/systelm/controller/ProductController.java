package com.systelm.controller;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateCategoryCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.UpdateProductCommand;
import com.systelm.entity.Product;
import com.systelm.entity.ProductBatch;
import com.systelm.entity.ProductCategory;
import com.systelm.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return productService.listProducts();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody CreateProductCommand cmd) {
        return productService.createProduct(cmd);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody UpdateProductCommand cmd) {
        return productService.updateProduct(id, cmd);
    }

    @GetMapping("/product-categories")
    public List<ProductCategory> listCategories() {
        return productService.listCategories();
    }

    @PostMapping("/product-categories")
    public ProductCategory createCategory(@RequestBody CreateCategoryCommand cmd) {
        return productService.createCategory(cmd);
    }

    @GetMapping("/products/{id}/batches")
    public List<ProductBatch> listBatches(@PathVariable Long id) {
        return productService.listBatches(id);
    }

    @PostMapping("/products/{id}/batches")
    public ProductBatch createBatch(@PathVariable Long id, @RequestBody CreateBatchRequest req) {
        return productService.createBatch(new CreateBatchCommand(id, req.batchNo(), req.productionDate()));
    }

    public record CreateBatchRequest(String batchNo, String productionDate) {}
}
