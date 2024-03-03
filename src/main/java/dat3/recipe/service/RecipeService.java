package dat3.recipe.service;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.dto.RecipeDtoMapper;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {


    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeDtoMapper recipeDtoMapper;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, RecipeDtoMapper recipeDtoMapper) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.recipeDtoMapper = recipeDtoMapper;
    }


    public List<RecipeDto> getRecipes(Optional<String> category) {
        List<Recipe> result = category.isPresent() ? this.recipeRepository.findByCategoryName(category.get()) : this.recipeRepository.findAll();

        return result.stream().map(recipeDtoMapper).toList();
    }

    public RecipeDto getSingleRecipe(int id) {
        Recipe recipe = this.recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Recipe found"));

        return this.recipeDtoMapper.apply(recipe);
    }

    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    public RecipeDto createRecipe(RecipeDto recipeDto) {
        if (recipeDto.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot provide the id for a new recipe");
        }

        Category category = categoryRepository.findFirstByName(recipeDto.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));


        Recipe newRecipe = new Recipe();
        updateRecipe(newRecipe, recipeDto, category);
        recipeRepository.save(newRecipe);

        return recipeDtoMapper.apply(newRecipe);
    }

    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    private void updateRecipe(Recipe original, RecipeDto r, Category category) {
        original.setName(r.name());
        original.setInstructions(r.instructions());
        original.setIngredients(r.ingredients());
        original.setThumb(r.thumb());
        original.setYouTube(r.youtube());
        original.setSource(r.source());
        original.setCategory(category);
    }

    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    public RecipeDto updateRecipe(RecipeDto recipeDto, int id) {
        Category category = categoryRepository.findFirstByName(recipeDto.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));


        Recipe recipeInDB = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe with id not found in DB"));

        updateRecipe(recipeInDB, recipeDto, category);
        recipeRepository.save(recipeInDB);

        return recipeDtoMapper.apply(recipeInDB);
    }

    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    public RecipeDto deleteRecipe(int id) {
        Recipe recipeInDB = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found in db"));
        RecipeDto dto = recipeDtoMapper.apply(recipeInDB);

        recipeInDB.setCategory(null);

        recipeRepository.delete(recipeInDB);
        return dto;
    }
}
