package com.restauranthub.category.exception;

/**
 * Domain exception thrown when an administrator attempts to delete a category
 * that is still referenced by one or more food menu items.
 */
public class CategoryInUseException extends RuntimeException {

    private final Long categoryId;

    public CategoryInUseException(Long categoryId) {
        super("This category still contains menu items. Move or remove those items before deleting the category, or deactivate the category instead.");
        this.categoryId = categoryId;
    }

    public CategoryInUseException(String message) {
        super(message);
        this.categoryId = null;
    }

    public Long getCategoryId() {
        return categoryId;
    }
}
