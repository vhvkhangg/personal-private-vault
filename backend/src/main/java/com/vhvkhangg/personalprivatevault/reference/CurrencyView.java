package com.vhvkhangg.personalprivatevault.reference;

/** Public read model for currency reference data. */
public record CurrencyView(String code, String name, String symbol, int decimalPlaces) {
}
