package pe.com.lacunza.system.restosys.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.service.TableService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:4200") // Permite peticiones desde Angular local
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    // GET /api/tables
    @GetMapping
    public ResponseEntity<List<RestaurantTable>> getTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    // PATCH /api/tables/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestaurantTable> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> mapBodyRequest) {

        RestaurantTable.TableStatus status = RestaurantTable.TableStatus.valueOf(mapBodyRequest.get("status"));
        RestaurantTable updatedTable = tableService.updateTableStatus(id, status);

        return ResponseEntity.ok(updatedTable);
    }
}
