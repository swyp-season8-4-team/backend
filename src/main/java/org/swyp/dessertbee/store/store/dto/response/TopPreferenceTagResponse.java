package org.swyp.dessertbee.store.store.dto.response;

public record TopPreferenceTagResponse(
        Long tagId,
        String name,
        int rank
) {}
