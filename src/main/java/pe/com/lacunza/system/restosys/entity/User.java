package pe.com.lacunza.system.restosys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.com.lacunza.system.restosys.dtos.Role;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Contraseña encriptada con BCrypt

    @Column(unique = true)
    private String pin; // PIN único de 4 o 6 dígitos para el POS

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- Métodos de UserDetails (Spring Security) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Getters y Setters propios...
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
