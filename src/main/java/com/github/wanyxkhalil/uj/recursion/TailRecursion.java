package com.github.wanyxkhalil.uj.recursion;

import java.util.stream.Stream;

/**
 * 尾递归调用包装
 */
public class TailRecursion {

    /**
     * 统一结构的方法,获得当前递归的下一个递归
     */
    public static <T> Pkg<T> call(final Pkg<T> nextFrame) {
        return nextFrame;
    }

    /**
     * 结束当前递归，重写对应的默认方法的值,完成状态改为true,设置最终返回结果,设置非法递归调用
     *
     * @param value 最终递归值
     * @return 一个isFinished状态true的尾递归, 外部通过调用接口的invoke方法及早求值, 启动递归求值。
     */
    public static <T> Pkg<T> done(T value) {
        return new Pkg<T>() {
            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public T getResult() {
                return value;
            }

            @Override
            public Pkg<T> apply() {
                throw new RuntimeException("end of recursion");
            }
        };
    }


    @FunctionalInterface
    public interface Pkg<T> {

        /**
         * 用于递归栈帧之间的连接,惰性求值
         */
        Pkg<T> apply();

        default boolean isFinished() {
            return false;
        }

        /**
         * 获得递归结果,只有在递归结束才能调用,这里默认给出异常,通过工具类的重写来获得值
         */
        default T getResult() {
            throw new RuntimeException("still recursive");
        }

        /**
         * 及早求值,执行一系列的递归,因为栈帧只有一个,所以使用findFirst获得最终的栈帧,接着调用getResult方法获得最终递归值
         */
        default T invoke() {
            return Stream.iterate(this, Pkg::apply)
                    .filter(Pkg::isFinished)
                    .findFirst()
                    .map(Pkg::getResult)
                    .orElseThrow(() -> new RuntimeException("some error"));
        }
    }
}
