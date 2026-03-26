package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.request.CategoryCreateRequest;
import com.fsa.franchise.product_service.dto.request.CategoryUpdateRequest;
import com.fsa.franchise.product_service.dto.response.CategoryResponse;
import com.fsa.franchise.product_service.entity.Category;
import com.fsa.franchise.product_service.mapper.CategoryMapper;
import com.fsa.franchise.product_service.repository.CategoryRepository;
import com.fsa.franchise.product_service.repository.ProductRepository;
import com.fsa.franchise.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Category name already exists");
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(generateSlug(request.getName()));
        category.setTopping(request.getIsTopping() != null ? request.getIsTopping() : false);
        category.setStatus(Category.CategoryStatus.ACTIVE);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Category name already exists");
            }
        }
        categoryMapper.updateEntity(category, request);
        category.setSlug(generateSlug(request.getName()));
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        validateDeletionHierarchy(category);
        softDeleteHierarchy(category);
    }

    private void validateDeletionHierarchy(Category category) {
        if (productRepository.existsByCategoryId(category.getId())) {
            throw new RuntimeException("Cannot delete: Category '" + category.getName()
                    + "' or its sub-categories are linked to existing products.");
        }

        if (category.getSubCategories() != null) {
            for (Category sub : category.getSubCategories()) {
                validateDeletionHierarchy(sub);
            }
        }
    }

    private void softDeleteHierarchy(Category category) {
        category.setStatus(Category.CategoryStatus.INACTIVE);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        if (category.getSubCategories() != null) {
            for (Category sub : category.getSubCategories()) {
                softDeleteHierarchy(sub);
            }
        }
    }

    private String generateSlug(String input) {
        if (input == null)
            return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getListCategories(Boolean isTopping, UUID parentId) {
        List<Category> categories = categoryRepository.findCategoriesByFilters(isTopping, parentId);
        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }
}