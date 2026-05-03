package com.jewel.image2jewel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "jewelry_items")
public class JewelryItem {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String category; // e.g., Ring, Necklace, Earrings

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    // Vector field for k-NN search
    @Field(type = FieldType.Auto) 
    private float[] imageVector;

    public JewelryItem() {
    }

    public JewelryItem(String id, String name, String category, String description, String imageUrl, float[] imageVector) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imageVector = imageVector;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float[] getImageVector() {
        return imageVector;
    }

    public void setImageVector(float[] imageVector) {
        this.imageVector = imageVector;
    }
}
