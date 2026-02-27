package com.devlawal.wealthsimpleatie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads successfully.
 */
@SpringBootTest(classes = AtieApplication.class)
@ActiveProfiles("test")
class WealthsimpleAtieApplicationTests {

    @Test
    void contextLoads() {
    }
}
