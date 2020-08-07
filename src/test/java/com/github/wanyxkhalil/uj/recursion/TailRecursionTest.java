package com.github.wanyxkhalil.uj.recursion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

/**
 * 以累加测试尾递归
 */
public class TailRecursionTest {

    /**
     * 累加类
     */
    private static class Accumulation {

        /**
         * 包装前
         */
        public static long before(long number) {
            return before(1L, number);
        }

        private static long before(final long accumulation, final long number) {
            if (number == 1L) {
                return accumulation;
            }
            return before(accumulation + number, number - 1);
        }

        /**
         * 包装后
         */
        public static long tr(final long number) {
            return tr(1L, number).invoke();
        }

        private static TailRecursion.Pkg<Long> tr(final long accumulation, final long number) {
            if (number == 1L) {
                return TailRecursion.done(accumulation);
            }
            return TailRecursion.call(() -> tr(accumulation + number, number - 1));
        }
    }

    /**
     * 时间统计
     */
    private static void time(Supplier<Long> supplier) {
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            long factorial = supplier.get();
            long time = System.currentTimeMillis() - start;

            System.out.println(time / 5 + " ms: " + factorial);
        }
    }

    /**
     * 包装前非尾递归
     */
    @Test
    public void testThrow() {
        Assertions.assertThrows(StackOverflowError.class, () -> time(() -> Accumulation.before(1000_0000)));
    }

    /**
     * 包装后为尾递归
     */
    @Test
    public void testNotThrow() {
        Assertions.assertDoesNotThrow(() -> time(() -> Accumulation.tr(1000_0000)));
    }

}
