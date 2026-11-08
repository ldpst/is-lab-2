package com.ldpst.service.util;

import java.util.Locale;

import com.ldpst.model.domain.Address;
import com.ldpst.model.dto.Command.AddressCommand;

public final class AddressComparison {
    private AddressComparison() {
    }

    public static boolean matches(Address existing, AddressCommand requested) {
        return normalize(existing.getStreet()).equals(normalize(requested.street()))
                && normalize(existing.getZipCode()).equals(normalize(requested.zipCode()));
    }

    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("(?U)\\s+", " ").strip().toLowerCase(Locale.ROOT);
    }
}
