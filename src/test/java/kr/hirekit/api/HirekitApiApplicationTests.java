package kr.hirekit.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@EnableAutoConfiguration(exclude = RedisAutoConfiguration.class)
class HirekitApiApplicationTests {

	@MockBean
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void contextLoads() {
	}

}
