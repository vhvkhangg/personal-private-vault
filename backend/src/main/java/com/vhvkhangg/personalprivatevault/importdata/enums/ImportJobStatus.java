package com.vhvkhangg.personalprivatevault.importdata.enums;

/**
 * Lifecycle status of an import job from the frozen Database Schema v1.
 */
public enum ImportJobStatus {
    CREATED,
    PARSED,
    VALIDATED,
    IMPORTED,
    FAILED,
    CANCELLED
}
