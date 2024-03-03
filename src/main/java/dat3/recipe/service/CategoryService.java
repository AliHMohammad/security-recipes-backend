package dat3.recipe.service;

import dat3.recipe.entity.Category;
import dat3.recipe.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
