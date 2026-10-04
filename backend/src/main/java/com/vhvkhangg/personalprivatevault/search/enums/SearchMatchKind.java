package com.vhvkhangg.personalprivatevault.search.enums;

/**
 * Normalized match kind indicating how a search result matched the query.
 */
public enum SearchMatchKind {
    PRIMARY_EXACT(600),
    PRIMARY_PREFIX(550),
    PRIMARY_SUBSTRING(500),
    SECONDARY_EXACT(450),
    SECONDARY_PREFIX(450),
    SECONDARY_SUBSTRING(400),
    SHORT_FUZZY(350),
    TAG(300),
    BODY(200);

    private final int rankBucket;

    SearchMatchKind(int rankBucket) {
        this.rankBucket = rankBucket;
    }

    public int rankBucket() {
        return rankBucket;
    }

    public static SearchMatchKind fromRankBucket(int rankBucket) {
        return switch (rankBucket) {
            case 600 -> PRIMARY_EXACT;
            case 550 -> PRIMARY_PREFIX;
            case 500 -> PRIMARY_SUBSTRING;
            case 450 -> SECONDARY_EXACT;
            case 400 -> SECONDARY_SUBSTRING;
            case 350 -> SHORT_FUZZY;
            case 300 -> TAG;
            case 200 -> BODY;
            default -> PRIMARY_SUBSTRING;
        };
    }
}
