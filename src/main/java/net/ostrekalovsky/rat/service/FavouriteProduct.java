package net.ostrekalovsky.rat.service;

import lombok.Data;

@Data
public class FavouriteProduct {

    private final String name;
    private final int count;
    private final int code;
}
