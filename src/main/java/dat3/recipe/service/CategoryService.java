package dat3.recipe.service;

import dat3.recipe.dto.CategoryDto;
import dat3.recipe.entity.Category;
import dat3.recipe.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {


    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public List<String> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map((category -> category.getName())).toList();
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Optional<Category> category = categoryRepository.findFirstByNameIgnoreCase(categoryDto.name());
        if (category.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This category already exists");

        Category newCategory = new Category();
        newCategory.setName(categoryDto.name());

        categoryRepository.save(newCategory);
        return new CategoryDto(newCategory.getId(), newCategory.getName());
    }
}
