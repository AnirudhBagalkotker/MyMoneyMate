package com.mymoneymate.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.mymoneymate.dao.CategoryDAO;
import com.mymoneymate.models.Category;
import com.mymoneymate.services.exceptions.ServiceException;
import com.mymoneymate.services.exceptions.ValidationException;

public class CategoryService {
	private final CategoryDAO categoryDAO;
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+$");

	public CategoryService() {
		this.categoryDAO = new CategoryDAO();
	}

	public Category createCategory(Integer userId, String name, Category.TransactionType type, String description)
			throws ServiceException {
		validateCategory(name, type);

		try {
			Category category = new Category(name, type, description);
			return categoryDAO.create(category);
		} catch (SQLException e) {
			throw new ServiceException("Error creating category", e);
		}
	}

	public Category updateCategory(Category category) throws ServiceException {
		validateCategory(category.getName(), category.getType());

		try {
			Optional<Category> existingCategory = categoryDAO.findById(category.getId());
			if (existingCategory.isEmpty()) {
				throw new ValidationException("Category not found.");
			}

			boolean updated = categoryDAO.update(category);
			if (!updated) {
				throw new ServiceException("Failed to update category.");
			}

			return category;
		} catch (SQLException e) {
			throw new ServiceException("Error updating category", e);
		}
	}

	public void deleteCategory(Integer categoryId) throws ServiceException {
		try {
			Optional<Category> existingCategory = categoryDAO.findById(categoryId);
			if (existingCategory.isEmpty()) {
				throw new ValidationException("Category not found.");
			}

			boolean deleted = categoryDAO.delete(categoryId);
			if (!deleted) {
				throw new ServiceException("Failed to delete category.");
			}
		} catch (SQLException e) {
			throw new ServiceException("Error deleting category", e);
		}
	}

	public Category getCategoryById(Integer categoryId) throws ServiceException {
		try {
			return categoryDAO.findById(categoryId).orElseThrow(() -> new ValidationException("Category not found."));
		} catch (SQLException e) {
			throw new ServiceException("Error retrieving category", e);
		}
	}

	public List<Category> getAllCategories() throws ServiceException {
		try {
			return categoryDAO.findAll();
		} catch (SQLException e) {
			throw new ServiceException("Error retrieving categories", e);
		}
	}

	public List<Category> getCategoriesByType(Category.TransactionType type) throws ServiceException {
		try {
			return categoryDAO.findByType(type);
		} catch (SQLException e) {
			throw new ServiceException("Error retrieving categories", e);
		}
	}

	public Optional<Category> getCategoriesByName(String name) throws ServiceException {
		try {
			return categoryDAO.findByName(name);
		} catch (SQLException e) {
			throw new ServiceException("Error retrieving categories", e);
		}
	}

	private void validateCategory(String name, Category.TransactionType type) throws ValidationException {
		if (name == null) {
			throw new ValidationException("Category name is required.");
		}
		if (name.length() > 50) {
			throw new ValidationException("Category name too Long.");
		}
		if (!CATEGORY_PATTERN.matcher(name).matches()) {
			throw new ValidationException("Invalid category name.");
		}
		if (type == null) {
			throw new ValidationException("Category type is required.");
		}
	}
}
