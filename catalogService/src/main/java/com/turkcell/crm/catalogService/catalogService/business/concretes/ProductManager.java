package com.turkcell.crm.catalogService.catalogService.business.concretes;

import com.turkcell.crm.catalogService.catalogService.business.abstracts.ProductService;
import com.turkcell.crm.catalogService.catalogService.business.dtos.requests.product.CreateProductRequest;
import com.turkcell.crm.catalogService.catalogService.business.dtos.requests.product.UpdateProductRequest;
import com.turkcell.crm.catalogService.catalogService.business.dtos.responses.product.CreatedProductResponse;
import com.turkcell.crm.catalogService.catalogService.business.dtos.responses.product.GetAllProductsResponse;
import com.turkcell.crm.catalogService.catalogService.business.dtos.responses.product.GetProductByIdResponse;
import com.turkcell.crm.catalogService.catalogService.business.dtos.responses.product.UpdatedProductResponse;
import com.turkcell.crm.catalogService.catalogService.core.mapping.ModelMapperService;
import com.turkcell.crm.catalogService.catalogService.dataAccess.abstracts.ProductRepository;
import com.turkcell.crm.catalogService.catalogService.entities.concretes.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class ProductManager implements ProductService {

    private ModelMapperService modelMapperService;
    private ProductRepository productRepository;

    @Override
    public CreatedProductResponse add(CreateProductRequest createProductRequest) {
        Product product = this.modelMapperService.forRequest().map(createProductRequest, Product.class);
        product.setCreatedDate(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return this.modelMapperService.forResponse().map(savedProduct, CreatedProductResponse.class);
    }

    @Override
    public List<GetAllProductsResponse> getAll() {
        List<Product> products = productRepository.findAll().stream().filter(Product::isActive).toList();
        return products.stream().map(
                product -> this.modelMapperService.forResponse().map(product, GetAllProductsResponse.class)
        ).toList();
    }

    @Override
    public GetProductByIdResponse getById(int id) {
        Product product = productRepository.findById(id).orElse(null);
        return this.modelMapperService.forResponse().map(product, GetProductByIdResponse.class);
    }

    @Override
    public UpdatedProductResponse update(UpdateProductRequest updateProductRequest) {
        Product product = productRepository.findById(updateProductRequest.getId()).orElse(null);
        modelMapperService.forUpdate().map(updateProductRequest, product);
        product.setUpdatedDate(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return this.modelMapperService.forResponse().map(savedProduct, UpdatedProductResponse.class);
    }

    @Override
    public void delete(int id) {
        Product product = productRepository.findById(id).orElse(null);
        product.setActive(false);
        product.setDeletedDate(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    public boolean isExist(int id) {
        return productRepository.existsById(id);
    }

}
