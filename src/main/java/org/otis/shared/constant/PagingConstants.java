package org.otis.shared.constant;

import java.util.Set;

public final class PagingConstants {

    private PagingConstants() {
    }

    public static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("ASC", "DESC");
    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_OFFSET = 0;
}
