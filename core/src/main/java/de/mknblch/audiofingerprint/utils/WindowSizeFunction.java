package de.mknblch.audiofingerprint.utils;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * @author mknblch
 */
public interface WindowSizeFunction extends IntBinaryOperator {

    static WindowSizeFunction andThen(WindowSizeFunction function, IntUnaryOperator operator) {
        return (l, r) -> operator.applyAsInt(function.applyAsInt(l, r));
    }

    static WindowSizeFunction adaptive(int wBottom, int wTop) {
        final int wd = wTop - wBottom + 1;
        return (i, l) -> wBottom + (int) (i * ((float) wd / l));
    }

}
