package dev.redgamer6427a.core.processing;

public class Math {
    public static int maxDivisions(long x, int n) {
        int count = 0;
        while (x % n == 0) { // divisible
            x /= n;
            count++;
        }
        return count;
    }
}
