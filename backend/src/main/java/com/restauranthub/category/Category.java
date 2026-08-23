package com.restauranthub.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Category entity representing a food/menu category in the restaurant system.
 * Mapped to the 'categories' table in MySQL via JPA / Hibernate.
 */
@Entity
@Table(name = "categories")
public class Category {

    /**
     * Unique identifier and primary key for the category.
     * GenerationType.IDENTITY delegates auto-incrementing ID creation to MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Display name of the category (e.g., "Starters", "Desserts", "Beverages").
     * Cannot be null in the database.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * URL-friendly unique identifier (e.g., "starters", "desserts", "beverages").
     * Cannot be null and must be unique across all categories.
     */
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    /**
     * Flag indicating whether the category is currently active/visible.
     * Cannot be null in the database.
     * Defaults to true for all newly instantiated Category objects.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Default no-argument constructor.
     * Required by the JPA specification for Hibernate to instantiate entity objects using reflection.
     */
    public Category() {
    }

    /**
     * Convenience constructor to create a new category with active defaulting to true.
     *
     * @param name display name of category
     * @param slug URL-friendly unique identifier
     */
    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.active = true;
    }

    /**
     * Full constructor to initialize all fields when creating a category.
     *
     * @param name display name of category
     * @param slug URL-friendly unique identifier
     * @param active whether category is active
     */
    public Category(String name, String slug, Boolean active) {
        this.name = name;
        this.slug = slug;
        this.active = (active != null) ? active : true;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // --- equals, hashCode, and toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", active=" + active +
                '}';
    }
}
