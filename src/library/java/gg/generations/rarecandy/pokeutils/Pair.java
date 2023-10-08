package gg.generations.rarecandy.pokeutils;

import java.util.Objects;

public final class Pair<A, B> {
    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A a() {
        return a;
    }

    public void a(A a) {
        this.a = a;
    }

    public B b() {
        return b;
    }

    public void b(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Pair) obj;
        return Objects.equals(this.a, that.a) &&
               Objects.equals(this.b, that.b);
    }

    @Override
    public String toString() {
        return "Pair[" +
               "a=" + a + ", " +
               "b=" + b + ']';
    }
}