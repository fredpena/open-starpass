package dev.fredpena.admin.data;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class AdminCatalogService {

    private final AdminSessionStore sessionStore;

    public AdminCatalogService(AdminSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public List<AdminUser> findAllUsers() {
        return sessionStore.findAllUsers();
    }

    public Optional<AdminUser> findUser(Long id) {
        return sessionStore.findUser(id);
    }

    public List<AdminPlanet> findAllPlanets() {
        return sessionStore.findAllPlanets();
    }

    public Optional<AdminPlanet> findPlanet(Long id) {
        return sessionStore.findPlanet(id);
    }

    public boolean usernameExists(String username, Long excludeId) {
        return sessionStore.usernameExists(username, excludeId);
    }

    public boolean planetNameExists(String name, Long excludeId) {
        return sessionStore.planetNameExists(name, excludeId);
    }

    public AdminUser saveUser(AdminUser user) {
        return sessionStore.saveUser(user);
    }

    public AdminPlanet savePlanet(AdminPlanet planet) {
        return sessionStore.savePlanet(planet);
    }

    public void deleteUser(Long id) {
        sessionStore.deleteUser(id);
    }

    public void deleteUsers(Collection<Long> ids) {
        sessionStore.deleteUsers(ids);
    }

    public void deletePlanet(Long id) {
        sessionStore.deletePlanet(id);
    }

    public void deletePlanets(Collection<Long> ids) {
        sessionStore.deletePlanets(ids);
    }

    public void deactivateUsers(Collection<Long> ids) {
        sessionStore.deactivateUsers(ids);
    }

    public AdminUser toggleFavorite(Long id) {
        return sessionStore.toggleFavorite(id);
    }

    public AdminPlanet togglePlanetFavorite(Long id) {
        return sessionStore.togglePlanetFavorite(id);
    }

    public AdminSidebarData getSidebarData() {
        return sessionStore.getSidebarData();
    }

    public int nextColorIndex() {
        return sessionStore.nextColorIndex();
    }
}
