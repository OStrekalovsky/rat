package net.ostrekalovsky.rat.service;

import lombok.Data;

import java.util.List;

@Data
public class FavouriteReport {

    private final List<ProductProjection> favourites;
}
