package oss.fosslight.api.annotation;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 모든 API 엔드포인트에 공통으로 적용되는 Swagger 응답 정의
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                code = 401,
                message = "인증 실패\n\n" +
                        "**에러 코드 (errorCode):**\n\n" +
                        "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
                        "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)",
                examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"errorCode\":\"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))
        ),
        @ApiResponse(
                code = 500,
                message = "서버 내부 오류\n\n",
                examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"Unknown error.\"}"))
        )
        // 필요한 경우 403(권한 없음) 등의 공통 에러를 여기에 추가할 수 있습니다.
})
public @interface ApiCommonResponses {
}