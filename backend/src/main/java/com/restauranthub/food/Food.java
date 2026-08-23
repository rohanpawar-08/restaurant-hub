package com.restauranthub.food;

import com.restauranthub.category.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Food entity representing a menu item in the restaurant system.
 * Mapped to the 'foods' table in MySQL via JPA / Hibernate.
 *
 * Relationship:
 * Many Food items belong to One Category (@ManyToOne).
 * The 'foods' table contains the foreign key column 'category_id'.
 */
@Entity
@Table(name = "foods")
public class Food {

    /**
     * Primary key auto-incremented by MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Display name of the food item (e.g., "Margherita Pizza").
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Detailed description of ingredients, preparation, or flavor profile.
     */
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    /**
     * Price of the food item.
     * Uses BigDecimal and MySQL DECIMAL(10, 2) to prevent floating-point rounding errors.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Customer rating on a scale of 0.0 to 5.0.
     * Stored as DECIMAL(3, 1) in MySQL.
     */
    @Column(name = "rating", nullable = false, precision = 3, scale = 1)
    private BigDecimal rating;

    /**
     * URL or relative asset path to the food item's image. Nullable.
     */
    @Column(name = "image")
    private String image;

    /**
     * Flag indicating whether the item is vegetarian (true) or non-vegetarian (false).
     */
    @Column(name = "veg", nullable = false)
    private Boolean veg;

    /**
     * Flag indicating if the food item is featured as "popular" or recommended.
     * Defaults to false.
     */
    @Column(name = "popular", nullable = false)
    private Boolean popular = false;

    /**
     * Availability flag. When false, the item is out of stock / hidden from ordering.
     * Defaults to true.
     */
    @Column(name = "available", nullable = false)
    private Boolean available = true;

    /**
     * Relational mapping: Many Food items reference One Category.
     *
     * - @ManyToOne: Defines the multiplicity.
     * - fetch = FetchType.LAZY: Instructs Hibernate NOT to query the categories table
     *   unless the category property is explicitly accessed, preventing unnecessary SQL joins.
     * - @JoinColumn(name = "category_id", nullable = false): Specifies the foreign key column
     *   in the 'foods' table pointing to 'categories.id'.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Default no-argument constructor required by JPA.
     */
    public Food() {
    }

    /**
     * Convenience constructor to create a food item with sensible defaults.
     */
    public Food(String name, String description, BigDecimal price, BigDecimal rating,
                String image, Boolean veg, Boolean popular, Boolean available, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.image = image;
        this.veg = veg;
        this.popular = (popular != null) ? popular : false;
        this.available = (available != null) ? available : true;
        this.category = category;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getVeg() {
        return veg;
    }

    public void setVeg(Boolean veg) {
        this.veg = veg;
    }

    public Boolean getPopular() {
        return popular;
    }

    public void setPopular(Boolean popular) {
        this.popular = popular;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(id, food.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Food{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", veg=" + veg +
                ", popular=" + popular +
                ", available=" + available +
                '}';
    }
}
