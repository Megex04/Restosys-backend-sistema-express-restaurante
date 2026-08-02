package pe.com.lacunza.system.restosys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.TableRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceImplTest {

    private static final Long TABLE_ID = 1L;

    @Mock
    private TableRepository tableRepository;

    @InjectMocks
    private TableServiceImpl tableServiceImpl;

    private RestaurantTable buildTable(Long id, String number, RestaurantTable.TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber(number);
        table.setCapacity(4);
        table.setStatus(status);
        return table;
    }

    // --- getAllTables ---

    @Test
    void getAllTables_conMesasExistentes_deberiaRetornarLaListaCompleta() {
        // Arrange
        List<RestaurantTable> mesas = List.of(
                buildTable(1L, "M1", RestaurantTable.TableStatus.available),
                buildTable(2L, "M2", RestaurantTable.TableStatus.dirty)
        );
        when(tableRepository.findAll()).thenReturn(mesas);

        // Act
        List<RestaurantTable> resultado = tableServiceImpl.getAllTables();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals(mesas, resultado);
        verify(tableRepository).findAll();
    }

    @Test
    void getAllTables_sinMesas_deberiaRetornarListaVacia() {
        // Arrange
        when(tableRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<RestaurantTable> resultado = tableServiceImpl.getAllTables();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(tableRepository).findAll();
    }

    // --- updateTableStatus: camino feliz ---

    @Test
    void updateTableStatus_aAvailable_deberiaActualizarYGuardarLaMesa() {
        // Arrange
        RestaurantTable mesaExistente = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.dirty);
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(mesaExistente));
        when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RestaurantTable resultado = tableServiceImpl.updateTableStatus(TABLE_ID, RestaurantTable.TableStatus.available);

        // Assert
        assertEquals(RestaurantTable.TableStatus.available, resultado.getStatus());
        assertEquals(RestaurantTable.TableStatus.available, mesaExistente.getStatus());
        verify(tableRepository).save(mesaExistente);
    }

    @ParameterizedTest
    @EnumSource(value = RestaurantTable.TableStatus.class, names = "occupied", mode = EnumSource.Mode.EXCLUDE)
    void updateTableStatus_conEstadoPermitido_deberiaActualizarCorrectamente(RestaurantTable.TableStatus nuevoEstado) {
        // Arrange
        RestaurantTable mesaExistente = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(mesaExistente));
        when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RestaurantTable resultado = tableServiceImpl.updateTableStatus(TABLE_ID, nuevoEstado);

        // Assert
        assertEquals(nuevoEstado, resultado.getStatus());
        verify(tableRepository).save(mesaExistente);
    }

    // --- updateTableStatus: casos de error ---

    @Test
    void updateTableStatus_conMesaInexistente_deberiaLanzarRuntimeExceptionYNoGuardar() {
        // Arrange
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tableServiceImpl.updateTableStatus(TABLE_ID, RestaurantTable.TableStatus.available));

        assertEquals("Mesa no encontrada con ID: " + TABLE_ID, exception.getMessage());
        verify(tableRepository, never()).save(any());
    }

    @Test
    void updateTableStatus_aOccupied_deberiaLanzarRuntimeExceptionYNoGuardar() {
        // Arrange
        RestaurantTable mesaExistente = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.available);
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(mesaExistente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tableServiceImpl.updateTableStatus(TABLE_ID, RestaurantTable.TableStatus.occupied));

        assertEquals("No se puede cambiar una mesa a occupied manualmente. Cree una orden primero.", exception.getMessage());
        assertEquals(RestaurantTable.TableStatus.available, mesaExistente.getStatus());
        verify(tableRepository, never()).save(any());
    }
}
