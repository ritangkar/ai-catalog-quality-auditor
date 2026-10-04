package com.ritangkar.catalogauditor.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritangkar.catalogauditor.model.CatalogProduct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Repository
public class CatalogRepository {

    private final List<CatalogProduct> products;

    public CatalogRepository() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("sample-data/catalog.json").getInputStream()) {
            CatalogProduct[] arr = mapper.readValue(in, CatalogProduct[].class);
            this.products = List.of(arr);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load bundled catalog", e);
        }
    }

    public List<CatalogProduct> findAll() {
        return products;
    }
}
