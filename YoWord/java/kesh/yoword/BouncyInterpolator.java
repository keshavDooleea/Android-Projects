package kesh.yoword;

import android.view.animation.Interpolator;

public class BouncyInterpolator implements Interpolator {

    private double amplitude = 1;
    private double frequency = 10;

    BouncyInterpolator(double amplitude, double frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) (-1 * Math.pow(Math.E, -input/amplitude)*Math.cos(frequency * input) + 1);
    }
}
