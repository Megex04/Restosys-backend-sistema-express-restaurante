package pe.com.lacunza.system.restosys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.lacunza.system.restosys.entity.Category;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.repository.CategoryRepository;
import pe.com.lacunza.system.restosys.repository.DishRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private MenuServiceImpl menuServiceImpl;

    private Category buildCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Dish buildDish(Long id, String name, boolean available) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(BigDecimal.valueOf(19.90));
        dish.setAvailable(available);
        return dish;
    }

    @Test
    void getAllCategories_conCategoriasExistentes_deberiaRetornarLaListaCompleta() {
        // Arrange
        List<Category> categorias = List.of(
                buildCategory(1L, "Entradas"),
                buildCategory(2L, "Platos de fondo")
        );
        when(categoryRepository.findAll()).thenReturn(categorias);

        // Act
        List<Category> resultado = menuServiceImpl.getAllCategories();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals(categorias, resultado);
        verify(categoryRepository).findAll();
        verifyNoInteractions(dishRepository);
    }

    @Test
    void getAllCategories_sinCategorias_deberiaRetornarListaVacia() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Category> resultado = menuServiceImpl.getAllCategories();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(categoryRepository).findAll();
    }

    @Test
    void getAvailableMenu_deberiaRetornarSoloLosPlatosDisponibles() {
        // Arrange
        List<Dish> disponibles = List.of(
                buildDish(1L, "Lomo saltado", true),
                buildDish(2L, "Ceviche", true)
        );
        when(dishRepository.findByAvailableTrue()).thenReturn(disponibles);

        // Act
        List<Dish> resultado = menuServiceImpl.getAvailableMenu();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals(disponibles, resultado);
        assertTrue(resultado.stream().allMatch(Dish::getAvailable));
        verify(dishRepository).findByAvailableTrue();
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getAvailableMenu_sinPlatosDisponibles_deberiaRetornarListaVacia() {
        // Arrange
        when(dishRepository.findByAvailableTrue()).thenReturn(Collections.emptyList());

        // Act
        List<Dish> resultado = menuServiceImpl.getAvailableMenu();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(dishRepository).findByAvailableTrue();
    }
}
