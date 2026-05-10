package pe.com.lacunza.system.restosys.service;

import pe.com.lacunza.system.restosys.dtos.AuthResponseDTO;
import pe.com.lacunza.system.restosys.dtos.RegisterRequest;

public interface AuthService {
    AuthResponseDTO register(RegisterRequest request);
}
