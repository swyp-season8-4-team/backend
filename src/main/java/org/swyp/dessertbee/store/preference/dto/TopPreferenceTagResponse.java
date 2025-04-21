package org.swyp.dessertbee.store.preference.dto;

public record TopPreferenceTagResponse(
        Long tagId,
        String name,
        int rank
) {}
