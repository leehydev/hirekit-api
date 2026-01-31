package kr.hirekit.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hirekit.api.client.dto.CorpOutlineApiResponse;
import kr.hirekit.api.config.PublicDataProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PublicDataClient {

    private final WebClient publicDataWebClient;
    private final PublicDataProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * 공공데이터 API GET 요청
     *
     * @param path            엔드포인트 경로 (예: "/B552584/ArpltnInforInqireSvc/...")
     * @param queryCustomizer 추가 쿼리 파라미터 설정
     * @return 응답 문자열
     */
    public String get(String path,
            java.util.function.Consumer<org.springframework.web.util.UriBuilder> queryCustomizer) {
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

    /**
     * 기업 개요 조회 (금융위원회 기업기본정보 V2)
     *
     * @param crno   법인등록번호
     * @param corpNm 법인명
     * @return API 응답 (타입 정의됨)
     */
    public CorpOutlineApiResponse getCorpOutline(String crno, String corpNm) {
        String json = get("/1160100/service/GetCorpBasicInfoService_V2/getCorpOutline_V2", uri -> {
            uri.queryParam("resultType", "json")
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 5);
            if (crno != null)
                uri.queryParam("crno", crno);
            if (corpNm != null)
                uri.queryParam("corpNm", corpNm);
        });
        try {
            return objectMapper.readValue(json, CorpOutlineApiResponse.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("기업개요 API 응답 파싱 실패", e);
        }
    }
}
