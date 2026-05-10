package pe.com.lacunza.system.restosys.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.lacunza.system.restosys.entity.Category;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @GetMapping("/dishes/available")
    public ResponseEntity<List<Dish>> getAvailableDishes() {
        return ResponseEntity.ok(menuService.getAvailableMenu());
    }
}
