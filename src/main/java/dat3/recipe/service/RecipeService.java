package dat3.recipe.service;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.dto.RecipeDtoMapper;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import dat3.security.entity.Role;
import dat3.security.entity.UserWithRoles;
import dat3.security.repository.UserWithRolesRepository;
import dat3.security.service.UserDetailsServiceImp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final UserWithRolesRepository userWithRolesRepository;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, RecipeDtoMapper recipeDtoMapper, UserWithRolesRepository userWithRolesRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.recipeDtoMapper = recipeDtoMapper;
        this.userWithRolesRepository = userWithRolesRepository;
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

    @Transactional
    public RecipeDto updateRecipe(RecipeDto recipeDto, int id, Principal principal) {
        Category category = categoryRepository.findFirstByNameIgnoreCase(recipeDto.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));

        Recipe recipeInDB = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe with id not found in DB"));

        UserWithRoles userInDB = userWithRolesRepository.findById(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> roles = userInDB.getRoles().stream().map((role) -> role.getRoleName()).toList();

        //Hvis du ikke er ADMIN...
        if (!roles.contains("ADMIN")) {

            //Og ej forfatter til oprettelsen af opskriften...
            if (recipeInDB.getOwner() == null || !recipeInDB.getOwner().equals(userInDB.getUsername())) {
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

        UserWithRoles userInDB = userWithRolesRepository.findById(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> roles = userInDB.getRoles().stream().map((role) -> role.getRoleName()).toList();

        //Hvis du ikke er ADMIN...
        if (!roles.contains("ADMIN")) {
            //og ej forfatter til oprettelsen af opskriften...
            if (recipeInDB.getOwner() == null || !recipeInDB.getOwner().equals(userInDB.getUsername())) {
                //Så returner UNAUTHORIZED
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized to delete recipes");
            }
        }

        recipeInDB.setCategory(null);
        recipeRepository.delete(recipeInDB);
        return dto;
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

    //En anden måde at hente rollerne på ud fra principal objektet.
    /*
    private Set<String> getPrincipalRoles(Principal principal) {
        Authentication authentication = (Authentication) principal;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Set<String> roles = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            roles.add(authority.getAuthority());
        }

        return roles;
    }
    */
}
