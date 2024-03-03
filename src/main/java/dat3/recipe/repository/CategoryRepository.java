package dat3.recipe.repository;

import dat3.recipe.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findFirstByName(String name);
}
