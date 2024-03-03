package dat3.recipe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dat3.recipe.entity.Recipe;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


public record RecipeDto(
        Integer id,
        String name,
        String category,
        String instructions,
        String ingredients,
        String youtube,
        String thumb,
        String source,
        LocalDateTime created,
        LocalDateTime edited
) {

}

