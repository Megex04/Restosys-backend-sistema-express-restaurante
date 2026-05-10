package pe.com.lacunza.system.restosys.service;

import pe.com.lacunza.system.restosys.entity.RestaurantTable;

import java.util.List;

public interface TableService {

    List<RestaurantTable> getAllTables();
    RestaurantTable updateTableStatus(Long id, RestaurantTable.TableStatus newStatus);
}
