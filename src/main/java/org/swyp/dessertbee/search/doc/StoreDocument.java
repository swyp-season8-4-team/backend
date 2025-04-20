package org.swyp.dessertbee.search.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDocument {

    @JsonProperty("storeId")
    private Long storeId;

    @JsonProperty("storeUuid")
    private UUID storeUuid;

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("tagNames")
    private List<String> tagNames;

    @JsonProperty("menuNames")
    private List<String> menuNames;

    @JsonProperty("deleted")
    private boolean deleted;
}