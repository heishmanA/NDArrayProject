package com.guard;

/**
 * Simple class to just guard against null entries, out of bounds, etc.
 *
 * @author Aren
 */

public class Guard {

    /**
     * Allow class to be instantiated.
     */
    public Guard() {

    }

    /**
     * Guards against null entries.
     *
     * @param <T> the type of value
     * @param x   the value to check if null
     * @throws NullPointerException if x == null
     */
    public <T> void againstNull(T x) throws NullPointerException {
        if (x == null) {
            throw new NullPointerException("Null Pointer Exception");
        }
    }

    /**
     * Guard against indexOutOfBound errors
     *
     * @param lowerBound the lower bounds to test against {@code x_lb}
     * @param upperBound the upper bounds to test against {@code x_ub}
     * @param x          the value that is to be in the range lowerBounds < x < upperBounds
     * @throws IndexOutOfBoundsException if x < lowerBounds || x > upperBounds
     */
    public void againstIndexOutOfBounds(int lowerBound, int upperBound, int x) throws IndexOutOfBoundsException {
        if (x < lowerBound || x > upperBound) {
            throw new IndexOutOfBoundsException("Index Out Of Bounds Error: " + x);
        }
    }

}
