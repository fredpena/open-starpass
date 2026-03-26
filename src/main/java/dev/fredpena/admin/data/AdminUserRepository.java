package dev.fredpena.admin.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    List<AdminUser> findAllByOrderByIdDesc();

    List<AdminUser> findByFavoriteTrueOrderByNameAsc();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
}
