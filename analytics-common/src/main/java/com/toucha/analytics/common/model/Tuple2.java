package com.toucha.analytics.common.model;

/**
 * This class design for two tuple that the function can return 2 tuple in once.
 * 
 * @author senhui.li
 * @param <A>
 * @param <B>
 */
public class Tuple2<A, B> {

    private final A first;
    private final B second;

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
