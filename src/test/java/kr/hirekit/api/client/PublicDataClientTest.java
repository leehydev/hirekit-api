package kr.hirekit.api.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.hirekit.api.client.dto.CorpOutlineApiResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PublicDataClientTest {

    @Autowired
    private PublicDataClient publicDataClient;

    @Test
    void getCorpOutline_법인등록번호로_조회() {
        CorpOutlineApiResponse result = publicDataClient.getCorpOutline("1101113892240", null);

        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void getCorpOutline_법인명으로_조회() {
        CorpOutlineApiResponse result = publicDataClient.getCorpOutline(null, "메리츠자산운용");

        assertNotNull(result);
        System.out.println(result);
    }
}
