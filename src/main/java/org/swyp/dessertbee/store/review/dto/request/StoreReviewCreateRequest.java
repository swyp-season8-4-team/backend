package org.swyp.dessertbee.store.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewCreateRequest {

    @NotNull(message = "유저 UUID는 필수입니다.")
    private UUID userUuid;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;

    @NotNull(message = "평점은 필수입니다.")
    @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    private BigDecimal rating;
}
