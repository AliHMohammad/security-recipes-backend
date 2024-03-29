package dat3.recipe.api;


import dat3.recipe.dto.RecipeDto;
import dat3.recipe.entity.Recipe;
import dat3.recipe.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/recipes")
public class RecipeController {


    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    //Get all
    @GetMapping
    public ResponseEntity<List<RecipeDto>> getRecipes(
            @RequestParam Optional<String> category
            ) {
        return ResponseEntity.ok(recipeService.getRecipes(category));
    }

    //Get single
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getSingleRecipe(@PathVariable("id") int id) {
        return ResponseEntity.ok(recipeService.getSingleRecipe(id));
    }

    //Create
    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(@RequestBody RecipeDto recipeDto, Principal p) {
        RecipeDto createdRecipe = recipeService.createRecipe(recipeDto, p);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRecipe.id())
                .toUri();

        return ResponseEntity.created(location).body(createdRecipe);
    }

    //Update
    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@RequestBody RecipeDto recipeDto, @PathVariable("id") int id, Principal principal) {
        return ResponseEntity.ok(recipeService.updateRecipe(recipeDto, id, principal));
    }

    //Delete
    @PreAuthorize("hasAnyAuthority('USER') or hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<RecipeDto> deleteRecipe(@PathVariable("id") int id, Principal principal) {
        return ResponseEntity.ok(recipeService.deleteRecipe(id, principal));
    }
}
