package pe.com.lacunza.system.restosys.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.com.lacunza.system.restosys.entity.Category;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.service.MenuService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @Test
    void getCategories() {
        // Arrange
        List<Category> listCategoryResponse = new ArrayList<>();
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        listCategoryResponse.add(category);

        when(menuService.getAllCategories()).thenReturn(listCategoryResponse);

        //Act
        ResponseEntity<List<Category>> response = menuController.getCategories();

        // Assert
        assertEquals(listCategoryResponse, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAvailableDishes() {
        // Arrange
        List<Dish> listDishResponse = new ArrayList<>();
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("Test Dish");
        listDishResponse.add(dish);

        when(menuService.getAvailableMenu()).thenReturn(listDishResponse);

        //Act
        ResponseEntity<List<Dish>> response = menuController.getAvailableDishes();

        // Assert
        assertEquals(listDishResponse, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}