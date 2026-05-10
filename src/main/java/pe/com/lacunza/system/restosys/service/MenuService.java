package pe.com.lacunza.system.restosys.service;

import pe.com.lacunza.system.restosys.entity.Category;
import pe.com.lacunza.system.restosys.entity.Dish;

import java.util.List;

public interface MenuService {
    List<Category> getAllCategories();

    List<Dish> getAvailableMenu();
}
