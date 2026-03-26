package dev.fredpena.admin.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminPlanetRepository extends JpaRepository<AdminPlanet, Long> {

    List<AdminPlanet> findAllByOrderByIdDesc();

    List<AdminPlanet> findByFavoriteTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
