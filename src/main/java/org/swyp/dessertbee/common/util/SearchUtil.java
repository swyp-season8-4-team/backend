package org.swyp.dessertbee.common.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SearchUtil {

    /**
     * 사용자의 검색어를 FULLTEXT BOOLEAN MODE에 적합한 형식으로 변환합니다.
     * - 모든 단어는 반드시 포함되어야 하며 접두사 일치가 되도록 설정됩니다.
     * - 예: "디저트 카페" → "+디저트* +카페*"
     */
    public static String toBooleanFulltextQuery(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.trim().isEmpty()) {
            return "";
        }

        return Arrays.stream(rawKeyword.trim().split("\\s+"))
                .filter(word -> !word.isBlank())
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));
    }
}
