package org.raytracerweb.math;

public class Tuple<T1 extends Object, T2 extends Object> {
    protected T1 a;
    protected T2 b;

    public Tuple(T1 a, T2 b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return the a
     */
    public T1 getA() {
        return a;
    }

    /**
     * @return the b
     */
    public T2 getB() {
        return b;
    }
}