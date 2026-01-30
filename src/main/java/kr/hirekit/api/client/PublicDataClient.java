package kr.hirekit.api.client;

import kr.hirekit.api.config.PublicDataProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PublicDataClient {

    private final WebClient publicDataWebClient;
    private final PublicDataProperties properties;

    /**
     * 공공데이터 API GET 요청
     *
     * @param path 엔드포인트 경로 (예: "/B552584/ArpltnInforInqireSvc/...")
     * @param queryCustomizer 추가 쿼리 파라미터 설정
     * @return 응답 JSON 문자열
     */
    public String get(String path, java.util.function.Consumer<org.springframework.web.util.UriBuilder> queryCustomizer) {
        return publicDataWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path)
                            .queryParam("serviceKey", properties.getServiceKey());
                    queryCustomizer.accept(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
