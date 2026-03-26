package dev.fredpena.admin.data;

import java.util.List;

public record AdminSidebarData(
        long userCount,
        long planetCount,
        List<FavoriteItem> favoriteItems
) {
    public record FavoriteItem(String label, String href, String kind) {
    }
}
