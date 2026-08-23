package com.restauranthub.category.exception;

/**
 * Domain exception thrown when a category creation or update violates the unique slug constraint.
 */
public class DuplicateCategorySlugException extends RuntimeException {

    public DuplicateCategorySlugException(String slug) {
        super("Category already exists with slug: '" + slug + "'");
    }
}
