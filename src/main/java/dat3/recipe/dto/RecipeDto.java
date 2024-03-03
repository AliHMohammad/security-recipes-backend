package dat3.recipe.dto;

import jakarta.persistence.Column;

public class RecipeDto {
    private String name;
    private String instructions;
    private String ingredients;

    private String thumb;
    private String youTube;
    private String source;

    public String getName() {
        return name;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getThumb() {
        return thumb;
    }

    public String getYouTube() {
        return youTube;
    }

    public String getSource() {
        return source;
    }
}
