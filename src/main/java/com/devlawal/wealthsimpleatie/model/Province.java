package com.devlawal.wealthsimpleatie.model;

/**
 * Canadian provinces and territories supported by the provincial policy engine.
 * Tax rates and thresholds are encoded per province in ProvincialTaxRates.
 */
public enum Province {
    AB("Alberta"),
    BC("British Columbia"),
    MB("Manitoba"),
    NB("New Brunswick"),
    NL("Newfoundland and Labrador"),
    NS("Nova Scotia"),
    NT("Northwest Territories"),
    NU("Nunavut"),
    ON("Ontario"),
    PE("Prince Edward Island"),
    QC("Quebec"),
    SK("Saskatchewan"),
    YT("Yukon");

    private final String displayName;

    Province(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
