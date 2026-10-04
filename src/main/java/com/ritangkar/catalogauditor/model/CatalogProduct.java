package com.ritangkar.catalogauditor.model;

public record CatalogProduct(
        String id,
        String name,
        String category,
        String brand,
        String color,
        double price,
        String material,
        String description
) {
}
