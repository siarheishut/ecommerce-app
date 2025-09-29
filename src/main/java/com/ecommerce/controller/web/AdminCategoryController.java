package com.ecommerce.controller.web;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.CategoryInUseException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @ModelAttribute("adminSection")
    public String adminSection() {
        return "categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "/admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName());
        model.addAttribute("category", categoryDto);
        return "/admin/category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") CategoryDto categoryDto,
                               BindingResult bindingResult) {
        try {
            categoryService.save(categoryDto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue(
                    "name", "category.name", "A category with this name already exists.");
            return "/admin/category-form";
        }
        return "redirect:/admin/categories/list";
    }

    @GetMapping("/list")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAllForAdmin();
        model.addAttribute("categories", categories);
        return "admin/categories-list";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteCategories(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete this category because it is currently assigned to one or more products.");
        } catch (CategoryInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Can't delete the category. It is in use.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the category.");
        }
        return "redirect:/admin/categories/list";
    }

    @PostMapping("/restore/{id}")
    public String restoreCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.restoreById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category restored successfully.");
        } catch (Exception e) {
            log.error("Error restoring category with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while restoring the category.");
        }
        return "redirect:/admin/categories/list";
    }
}
