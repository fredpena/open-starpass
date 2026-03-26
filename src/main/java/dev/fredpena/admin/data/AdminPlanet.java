package dev.fredpena.admin.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_planets")
public class AdminPlanet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String climate;

    @Column(name = "gravity_pct", nullable = false)
    private int gravityPct;

    @Column(name = "distance_label", nullable = false)
    private String distanceLabel;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    @Column(nullable = false, length = 600)
    private String summary;

    @Column(nullable = false)
    private boolean favorite;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClimate() {
        return climate;
    }

    public void setClimate(String climate) {
        this.climate = climate;
    }

    public int getGravityPct() {
        return gravityPct;
    }

    public void setGravityPct(int gravityPct) {
        this.gravityPct = gravityPct;
    }

    public String getDistanceLabel() {
        return distanceLabel;
    }

    public void setDistanceLabel(String distanceLabel) {
        this.distanceLabel = distanceLabel;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
