package com.airlinemanagementsystem.booking.entity;

public enum LuggageStatus {
    BOOKED,          // Paid for, but not at airport
    CHECKED_IN,      // Dropped at counter
    SECURITY_CLEARED,// Passed X-Ray
    LOADED,          // On the plane
    UNLOADED,        // Off the plane
    ON_CAROUSEL,     // At destination belt
    CLAIMED,         // Customer picked it up
    LOST             // Alert state
}
