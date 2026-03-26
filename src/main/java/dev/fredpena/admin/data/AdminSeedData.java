package dev.fredpena.admin.data;

import java.time.LocalDate;
import java.util.List;

final class AdminSeedData {

    private AdminSeedData() {
    }

    static List<AdminUser> users() {
        return List.of(
                user(1L, "@emma", "EE", "Emma Executive", "Software Engineer", "emma.executive@starpass.net", "+123 456 7890", "Moon Base One", "Active", LocalDate.parse("2022-08-15"), 1, true),
                user(2L, "@alice", "AS", "Alice Smith", "Data Scientist", "alice.smith@apex.net", "+123 456 7881", "New London Station", "Active", LocalDate.parse("2023-05-20"), 2, true),
                user(3L, "@bob", "BJ", "Bob Johnson", "Product Manager", "bob.johnson@starpass.net", "+123 456 7812", "Mars Dock Alpha", "Active", LocalDate.parse("2024-01-10"), 3, false),
                user(4L, "@emily", "ED", "Emily Davis", "UX Designer", "emily.davis@gmail.com", "+123 456 7802", "Europa Hub", "Active", LocalDate.parse("2022-11-28"), 4, false),
                user(5L, "@michael", "MB", "Michael Brown", "Marketing Specialist", "michael.brown@apex.net", "+123 456 7895", "Orbital City", "Active", LocalDate.parse("2023-09-05"), 5, false),
                user(6L, "@sophia", "SM", "Sophia Miller", "Financial Analyst", "sophia.miller@gmail.com", "+123 456 7866", "Titan Port", "Active", LocalDate.parse("2022-07-12"), 1, false),
                user(7L, "@david", "DT", "David Taylor", "Project Manager", "david.taylor@starpass.net", "+123 456 7844", "Earth HQ", "Active", LocalDate.parse("2023-03-02"), 2, false)
        );
    }

    static List<AdminPlanet> planets() {
        return List.of(
                planet(1L, "Mercury", "Extremely hot and cold, barren", 38, "91M km", "#6b6b6b",
                        "A scorched rocky world with dramatic temperature shifts and almost no atmosphere.", false),
                planet(2L, "Venus", "Hot, thick and acidic atmosphere", 91, "41M km", "#c79c4a",
                        "Dense clouds and relentless heat make Venus a dramatic but hostile destination.", false),
                planet(3L, "Earth", "Diverse, temperate and habitable", 100, "0M km", "#4a7cf7",
                        "The operational baseline for gravity, climate complexity and interplanetary logistics.", true),
                planet(4L, "Mars", "Cold, dusty and thin atmosphere", 38, "78M km", "#c1440e",
                        "A frontier planet known for research hubs, red terrain and ambitious colonization plans.", true),
                planet(5L, "Jupiter", "Stormy gas giant with no solid surface", 253, "629M km", "#c88b3a",
                        "Massive atmospheric bands and extreme pressure systems define this giant world.", false),
                planet(6L, "Saturn", "Windy gas giant with icy rings", 107, "1.2B km", "#e4d191",
                        "A visually iconic planet whose ring systems make it a premium sightseeing route.", false),
                planet(7L, "Kepler-22b", "Temperate and potentially habitable", 120, "620 LY", "#4a90d9",
                        "A long-range candidate for future exploration with a softer climate profile than most exoplanets.", false),
                planet(8L, "Proxima b", "Tidally locked with extreme radiation", 130, "4.2 LY", "#9b59b6",
                        "Compact, mysterious and difficult, this exoplanet attracts high-risk scientific missions.", false),
                planet(9L, "Titan", "Dense atmosphere with methane lakes", 14, "1.2B km", "#d4a843",
                        "Saturn's moon offers a rare atmospheric environment and a unique amber visual identity.", false),
                planet(10L, "Europa", "Icy crust with subsurface ocean", 13, "629M km", "#b0c4de",
                        "An ice-covered destination prized for its research value and oceanic potential below the surface.", false)
        );
    }

    private static AdminUser user(Long id, String username, String initials, String name, String role, String email,
                                  String phone, String location, String status, LocalDate joinedOn,
                                  int colorIndex, boolean favorite) {
        AdminUser user = new AdminUser();
        user.setId(id);
        user.setUsername(username);
        user.setInitials(initials);
        user.setName(name);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);
        user.setLocation(location);
        user.setStatus(status);
        user.setJoinedOn(joinedOn);
        user.setColorIndex(colorIndex);
        user.setFavorite(favorite);
        return user;
    }

    private static AdminPlanet planet(Long id, String name, String climate, int gravityPct, String distanceLabel,
                                      String colorHex, String summary, boolean favorite) {
        AdminPlanet planet = new AdminPlanet();
        planet.setId(id);
        planet.setName(name);
        planet.setClimate(climate);
        planet.setGravityPct(gravityPct);
        planet.setDistanceLabel(distanceLabel);
        planet.setColorHex(colorHex);
        planet.setSummary(summary);
        planet.setFavorite(favorite);
        return planet;
    }
}
