package pe.com.lacunza.system.restosys.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.service.TableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableControllerTest {

    @Mock
    private TableService tableService;

    @InjectMocks
    private TableController tableController;

    @Test
    void getTables() {
        // Arrange
        List<RestaurantTable> restaurantTableList = new ArrayList<>();
        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setNumber("Test Table");
        restaurantTableList.add(table);

        when(tableService.getAllTables()).thenReturn(restaurantTableList);

        //Act
        ResponseEntity<List<RestaurantTable>> response = tableController.getTables();

        // Assert
        assertEquals(restaurantTableList, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateStatus() {
        // Arrange
        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setNumber("Test Table");

        Map<String, String> mapBodyRequest = Map.of("status", "available");

        when(tableService.updateTableStatus(anyLong(), any(RestaurantTable.TableStatus.class))).thenReturn(table);

        //Act
        ResponseEntity<RestaurantTable> response = tableController.updateStatus(1L, mapBodyRequest);

        // Assert
        assertEquals(table, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}