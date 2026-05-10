package pe.com.lacunza.system.restosys.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.TableRepository;
import pe.com.lacunza.system.restosys.service.TableService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    @Override
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public RestaurantTable updateTableStatus(Long id, RestaurantTable.TableStatus newStatus) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        if (newStatus == RestaurantTable.TableStatus.occupied) {
            throw new RuntimeException("No se puede cambiar una mesa a occupied manualmente. Cree una orden primero.");
        }

        table.setStatus(newStatus);
        return tableRepository.save(table);
    }
}
