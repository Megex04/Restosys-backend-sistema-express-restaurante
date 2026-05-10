package pe.com.lacunza.system.restosys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Una categoría tiene muchos platillos
    @OneToMany(mappedBy = "category")
    @JsonIgnore // Evita bucles infinitos al convertir a JSON
    private List<Dish> dishes;
}
