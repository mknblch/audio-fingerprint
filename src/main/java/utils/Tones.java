package utils;

/**
 * @author mknblch
 */
public enum Tones {

    C(frequency(440, -9, 0)),
    Cs(frequency(440, -8, 0)),
    D(frequency(440, -7, 0)),
    Ds(frequency(440, -6, 0)),
    E(frequency(440, -5, 0)),
    F(frequency(440, -4, 0)),
    Fs(frequency(440, -3, 0)),
    G(frequency(440, -2, 0)),
    Gs(frequency(440, -1, 0)),
    A(frequency(440, 0, 0)),
    As(frequency(440, -1, 0));

    public static final double BASE = 1.059463094359;

    public final double base;

    Tones(double base) {
        this.base = base;
    }

    public double shift(int shift) {
        return octaveShift(base, shift);
    }

    private static double octaveShift(double base, int shift) {
        if (shift < 0) {
            for (int i = shift; i < 0; i++) {
                base /= 2.0;
            }
        } else if (shift > 0) {
            for (int i = 0; i < shift; i++) {
                base *= 2.0;
            }
        }
        return base;
    }

    public static double frequency(double base, int toneShift, int octaveShift) {
        return octaveShift(base, octaveShift) * Math.pow(BASE, toneShift);
    }

}
