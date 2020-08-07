package com.github.wanyxkhalil.uj.id;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SnowflakeTest {

    Snowflake snowflake = Snowflake.getInstance(0, 0, 0, 0, 1577808000000L);

    @Test
    public void one() {
        Assertions.assertTrue(snowflake.next() > 0);
    }

    @Test
    public void multi() {
        for (int i = 0; i < (1 << 12); i++) {
            snowflake.next();
        }
    }
}
