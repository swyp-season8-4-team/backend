package org.swyp.dessertbee.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Swagger 문서의 에러 응답을 커스터마이징하는 설정 클래스
 * ApiErrorResponses 어노테이션을 처리용도 에러 코드별 응답 예제를 Swagger 문서에 자동으로 추가
 */

@Configuration
public class SwaggerErrorResponseCustomizer {

    @Bean
    public OperationCustomizer errorResponseOperationCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorResponses annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ApiErrorResponses.class);
            if (annotation != null) {
                for (ErrorCode errorCode : annotation.value()) {
                    String status = String.valueOf(errorCode.getHttpStatus().value());
                    ApiResponse response = getOrCreateApiResponse(operation, status);
                    MediaType mediaType = getOrCreateMediaType(response);
                    Map<String, Example> examples = getOrCreateExamples(mediaType);
                    // 에러 코드의 enum 이름을 key로 사용
                    examples.put(errorCode.name(), createErrorExample(errorCode));
                }
            }
            return operation;
        };
    }

    /**
     * 지정된 상태 코드에 대한 ApiResponse 객체를 가져오거나 새로 생성한다.
     */
    private ApiResponse getOrCreateApiResponse(Operation operation, String status) {
        ApiResponse apiResponse = operation.getResponses().get(status);
        if (apiResponse == null) {
            apiResponse = new ApiResponse().description("에러 응답");
            apiResponse.setContent(new Content());
            operation.getResponses().addApiResponse(status, apiResponse);
        }
        return apiResponse;
    }

    /**
     * ApiResponse에서 MediaType 객체를 가져오거나 새로 생성
     */
    private MediaType getOrCreateMediaType(ApiResponse response) {
        Content content = response.getContent();
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = new MediaType();
            content.addMediaType("application/json", mediaType);
        }
        return mediaType;
    }

    /**
     * MediaType에서 examples 맵을 가져오거나 새로 생성
     */
    private Map<String, Example> getOrCreateExamples(MediaType mediaType) {
        Map<String, Example> examples = mediaType.getExamples();
        if (examples == null) {
            examples = new HashMap<>();
            mediaType.setExamples(examples);
        }
        return examples;
    }

    /**
     * 에러 코드에 대한 예제 객체를 생성
     */
    private Example createErrorExample(ErrorCode errorCode) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("status", errorCode.getHttpStatus().value());
        example.put("code", errorCode.getCode());
        example.put("message", errorCode.getMessage());
        // API 문서에서는 고정된 예시 값 사용 권장 (여기서는 현재 시간 사용)
        example.put("timestamp", "2025-03-21T22:28:10.174078");
        return new Example().value(example);
    }
}
