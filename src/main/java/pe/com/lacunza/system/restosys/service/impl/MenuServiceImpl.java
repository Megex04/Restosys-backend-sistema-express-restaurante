package pe.com.lacunza.system.restosys.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.lacunza.system.restosys.entity.Category;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.repository.CategoryRepository;
import pe.com.lacunza.system.restosys.repository.DishRepository;
import pe.com.lacunza.system.restosys.service.MenuService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Dish> getAvailableMenu() {
        return dishRepository.findByAvailableTrue();
    }
}
