package pe.com.lacunza.system.restosys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.lacunza.system.restosys.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPin(String pin);
}
