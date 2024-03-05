package dat3.recipe.service;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.dto.RecipeDtoMapper;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import dat3.security.service.PrincipalService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;

@Service
public class RecipeService {


    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeDtoMapper recipeDtoMapper;
    private final PrincipalService principalService;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, RecipeDtoMapper recipeDtoMapper, PrincipalService principalService) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.recipeDtoMapper = recipeDtoMapper;
        this.principalService = principalService;
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


    public RecipeDto createRecipe(RecipeDto recipeDto, Principal p) {
        if (recipeDto.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot provide the id for a new recipe");
        }

        Category category = categoryRepository.findFirstByNameIgnoreCase(recipeDto.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));


        Recipe newRecipe = new Recipe();
        newRecipe.setOwner(p.getName());
        updateRecipe(newRecipe, recipeDto, category);
        recipeRepository.save(newRecipe);

        return recipeDtoMapper.apply(newRecipe);
    }


    private void updateRecipe(Recipe original, RecipeDto r, Category category) {
        original.setName(r.name());
        original.setInstructions(r.instructions());
        original.setIngredients(r.ingredients());
        original.setThumb(r.thumb());
        original.setYouTube(r.youtube());
        original.setSource(r.source());
        original.setCategory(category);
    }


    public RecipeDto updateRecipe(RecipeDto recipeDto, int id, Principal principal) {
        Category category = categoryRepository.findFirstByNameIgnoreCase(recipeDto.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));

        Recipe recipeInDB = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe with id not found in DB"));

        //Hvis du ikke er ADMIN...
        if (!principalService.getPrincipalRoles(principal).contains("ADMIN")) {

            //Og ej forfatter til oprettelsen af opskriften...
            if (recipeInDB.getOwner() == null || !recipeInDB.getOwner().equals(principal.getName())) {
                //Så returner UNAUTHORIZED
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized to update recipes");
            }
        }


        updateRecipe(recipeInDB, recipeDto, category);
        recipeRepository.save(recipeInDB);

        return recipeDtoMapper.apply(recipeInDB);
    }


    @Transactional
    public RecipeDto deleteRecipe(int id, Principal principal) {
        Recipe recipeInDB = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found in db"));
        RecipeDto dto = recipeDtoMapper.apply(recipeInDB);

        //Hvis du ikke er ADMIN...
        if (!principalService.getPrincipalRoles(principal).contains("ADMIN")) {

            //Og ej forfatter til oprettelsen af opskriften...
            if (recipeInDB.getOwner() == null || !recipeInDB.getOwner().equals(principal.getName())) {
                //Så returner UNAUTHORIZED
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized to delete recipes");
            }
        }


        recipeInDB.setCategory(null);
        recipeRepository.delete(recipeInDB);
        return dto;
    }

    //Rykket til et særskilt service lag som @Component

    /*private Set<String> getPrincipalRoles(Principal principal) {
        Authentication authentication = (Authentication) principal;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Set<String> roles = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            roles.add(authority.getAuthority());
        }

        return roles;
    }*/
}
