package dev.fredpena.admin.data;

public class AdminPlanet {

    private Long id;
    private String name;
    private String climate;
    private int gravityPct;
    private String distanceLabel;
    private String colorHex;
    private String summary;
    private boolean favorite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
