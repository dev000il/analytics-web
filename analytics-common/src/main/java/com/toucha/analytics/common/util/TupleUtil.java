package com.toucha.analytics.common.util;

import com.toucha.analytics.common.model.Tuple2;

public class TupleUtil {

    public static <A, B> Tuple2<A, B> tuple2(A a, B b) {
        return new Tuple2<A, B>(a, b);
    }
}
