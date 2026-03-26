package dev.fredpena.admin.data;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class AdminCatalogService {

    private final AdminUserRepository adminUserRepository;
    private final AdminPlanetRepository adminPlanetRepository;

    public AdminCatalogService(AdminUserRepository adminUserRepository, AdminPlanetRepository adminPlanetRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminPlanetRepository = adminPlanetRepository;
    }

    public List<AdminUser> findAllUsers() {
        return adminUserRepository.findAllByOrderByIdDesc();
    }

    public Optional<AdminUser> findUser(Long id) {
        return adminUserRepository.findById(id);
    }

    public List<AdminPlanet> findAllPlanets() {
        return adminPlanetRepository.findAllByOrderByIdDesc();
    }

    public Optional<AdminPlanet> findPlanet(Long id) {
        return adminPlanetRepository.findById(id);
    }

    public boolean usernameExists(String username, Long excludeId) {
        if (excludeId == null) {
            return adminUserRepository.existsByUsernameIgnoreCase(username);
        }
        return adminUserRepository.existsByUsernameIgnoreCaseAndIdNot(username, excludeId);
    }

    public boolean planetNameExists(String name, Long excludeId) {
        if (excludeId == null) {
            return adminPlanetRepository.existsByNameIgnoreCase(name);
        }
        return adminPlanetRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
    }

    @Transactional
    public AdminUser saveUser(AdminUser user) {
        return adminUserRepository.save(user);
    }

    @Transactional
    public AdminPlanet savePlanet(AdminPlanet planet) {
        return adminPlanetRepository.save(planet);
    }

    @Transactional
    public void deleteUser(Long id) {
        adminUserRepository.deleteById(id);
    }

    @Transactional
    public void deleteUsers(Collection<Long> ids) {
        adminUserRepository.deleteAllById(ids);
    }

    @Transactional
    public void deletePlanet(Long id) {
        adminPlanetRepository.deleteById(id);
    }

    @Transactional
    public void deletePlanets(Collection<Long> ids) {
        adminPlanetRepository.deleteAllById(ids);
    }

    @Transactional
    public void deactivateUsers(Collection<Long> ids) {
        adminUserRepository.findAllById(ids).forEach(user -> user.setStatus("Inactive"));
    }

    @Transactional
    public AdminUser toggleFavorite(Long id) {
        AdminUser user = adminUserRepository.findById(id).orElseThrow();
        user.setFavorite(!user.isFavorite());
        return user;
    }

    @Transactional
    public AdminPlanet togglePlanetFavorite(Long id) {
        AdminPlanet planet = adminPlanetRepository.findById(id).orElseThrow();
        planet.setFavorite(!planet.isFavorite());
        return planet;
    }

    public AdminSidebarData getSidebarData() {
        List<AdminSidebarData.FavoriteItem> userFavorites = adminUserRepository.findByFavoriteTrueOrderByNameAsc().stream()
                .map(user -> new AdminSidebarData.FavoriteItem(user.getName(), "admin/users/" + user.getId(), "user"))
                .toList();
        List<AdminSidebarData.FavoriteItem> planetFavorites = adminPlanetRepository.findByFavoriteTrueOrderByNameAsc().stream()
                .map(planet -> new AdminSidebarData.FavoriteItem(planet.getName(), "admin/planets/" + planet.getId(), "planet"))
                .toList();
        List<AdminSidebarData.FavoriteItem> favorites = new java.util.ArrayList<>();
        favorites.addAll(userFavorites);
        favorites.addAll(planetFavorites);

        return new AdminSidebarData(
                adminUserRepository.count(),
                adminPlanetRepository.count(),
                favorites
        );
    }

    public int nextColorIndex() {
        return (int) ((adminUserRepository.count() % 5) + 1);
    }
}
