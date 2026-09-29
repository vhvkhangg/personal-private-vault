package com.vhvkhangg.personalprivatevault.account.enums;

/**
 * Status indicating whether a target account follows the owner account, from frozen Database Schema v1.
 */
public enum FollowerStatus {
    CURRENT_FOLLOWER,
    NO_LONGER_FOLLOWING,
    NOT_FOLLOWING,
    UNKNOWN
}
