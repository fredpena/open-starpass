package dev.fredpena.admin.data;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSessionStore {

    private final Map<Long, AdminUser> usersById = new LinkedHashMap<>();
    private final Map<Long, AdminPlanet> planetsById = new LinkedHashMap<>();
    private long nextUserId;
    private long nextPlanetId;

    public AdminSessionStore() {
        AdminSeedData.users().forEach(user -> usersById.put(user.getId(), user));
        AdminSeedData.planets().forEach(planet -> planetsById.put(planet.getId(), planet));
        nextUserId = usersById.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1L;
        nextPlanetId = planetsById.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1L;
    }

    public List<AdminUser> findAllUsers() {
        return usersById.values().stream()
                .sorted(Comparator.comparing(AdminUser::getId).reversed())
                .toList();
    }

    public Optional<AdminUser> findUser(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public List<AdminPlanet> findAllPlanets() {
        return planetsById.values().stream()
                .sorted(Comparator.comparing(AdminPlanet::getId).reversed())
                .toList();
    }

    public Optional<AdminPlanet> findPlanet(Long id) {
        return Optional.ofNullable(planetsById.get(id));
    }

    public boolean usernameExists(String username, Long excludeId) {
        String normalized = username.toLowerCase(Locale.ROOT);
        return usersById.values().stream()
                .anyMatch(user -> user.getUsername() != null
                        && user.getUsername().toLowerCase(Locale.ROOT).equals(normalized)
                        && !user.getId().equals(excludeId));
    }

    public boolean planetNameExists(String name, Long excludeId) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return planetsById.values().stream()
                .anyMatch(planet -> planet.getName() != null
                        && planet.getName().toLowerCase(Locale.ROOT).equals(normalized)
                        && !planet.getId().equals(excludeId));
    }

    public AdminUser saveUser(AdminUser user) {
        if (user.getId() == null) {
            user.setId(nextUserId++);
        }
        usersById.put(user.getId(), user);
        return user;
    }

    public AdminPlanet savePlanet(AdminPlanet planet) {
        if (planet.getId() == null) {
            planet.setId(nextPlanetId++);
        }
        planetsById.put(planet.getId(), planet);
        return planet;
    }

    public void deleteUser(Long id) {
        usersById.remove(id);
    }

    public void deleteUsers(Collection<Long> ids) {
        ids.forEach(usersById::remove);
    }

    public void deletePlanet(Long id) {
        planetsById.remove(id);
    }

    public void deletePlanets(Collection<Long> ids) {
        ids.forEach(planetsById::remove);
    }

    public void deactivateUsers(Collection<Long> ids) {
        ids.stream()
                .map(usersById::get)
                .filter(java.util.Objects::nonNull)
                .forEach(user -> user.setStatus("Inactive"));
    }

    public AdminUser toggleFavorite(Long id) {
        AdminUser user = findUser(id).orElseThrow();
        user.setFavorite(!user.isFavorite());
        return user;
    }

    public AdminPlanet togglePlanetFavorite(Long id) {
        AdminPlanet planet = findPlanet(id).orElseThrow();
        planet.setFavorite(!planet.isFavorite());
        return planet;
    }

    public AdminSidebarData getSidebarData() {
        List<AdminSidebarData.FavoriteItem> favorites = new ArrayList<>();
        usersById.values().stream()
                .filter(AdminUser::isFavorite)
                .sorted(Comparator.comparing(AdminUser::getName, String.CASE_INSENSITIVE_ORDER))
                .map(user -> new AdminSidebarData.FavoriteItem(user.getName(), "admin/users/" + user.getId(), "user"))
                .forEach(favorites::add);
        planetsById.values().stream()
                .filter(AdminPlanet::isFavorite)
                .sorted(Comparator.comparing(AdminPlanet::getName, String.CASE_INSENSITIVE_ORDER))
                .map(planet -> new AdminSidebarData.FavoriteItem(planet.getName(), "admin/planets/" + planet.getId(), "planet"))
                .forEach(favorites::add);

        return new AdminSidebarData(usersById.size(), planetsById.size(), favorites);
    }

    public int nextColorIndex() {
        return (usersById.size() % 5) + 1;
    }
}
