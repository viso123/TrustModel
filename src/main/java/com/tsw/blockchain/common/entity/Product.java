package com.tsw.blockchain.common.entity;

public class Product {
    private String name;
    private String identifier;
    private String description;

    public Product(String name, String identifier, String description) {
        this.name = name;
        this.identifier = identifier;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
