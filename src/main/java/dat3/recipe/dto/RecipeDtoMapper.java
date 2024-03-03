package dat3.recipe.dto;

import dat3.recipe.entity.Recipe;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class RecipeDtoMapper implements Function<Recipe, RecipeDto> {


    @Override
    public RecipeDto apply(Recipe recipe) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getCategory().getName(),
                recipe.getInstructions(),
                recipe.getIngredients(),
                recipe.getYouTube(),
                recipe.getThumb(),
                recipe.getSource(),
                recipe.getCreated(),
                recipe.getEdited()
        );

    }
}
