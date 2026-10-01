package com.vhvkhangg.personalprivatevault.importdata.enums;

/**
 * Lifecycle status of an individual imported item from the frozen Database Schema v1.
 */
public enum ImportItemStatus {
    VALID,
    DUPLICATE,
    INVALID,
    IMPORTED,
    UPDATED,
    SKIPPED
}
