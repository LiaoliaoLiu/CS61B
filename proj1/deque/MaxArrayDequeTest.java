package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MaxArrayDequeTest {

    public static class DefaultComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    }

    @Test
    public void enhancedForLoopTest() {
        MaxArrayDeque<Integer> ma = new MaxArrayDeque<>(new DefaultComparator());

        ma.addLast(1);
        ma.addLast(2);
        ma.addLast(3);

        int sum = 0;
        for (Integer i : ma) {
            sum += i;
        }
        assertEquals(sum, 6);
    }

    @Test
    public void maxWithoutArgsTest() {
        MaxArrayDeque<Integer> ma = new MaxArrayDeque<>(new DefaultComparator());

        ma.addLast(1);
        ma.addLast(2);
        ma.addLast(3);
        Integer max = 3;
        assertEquals(ma.max(), max);
    }

    @Test
    public void randomizedTest() {
        MaxArrayDeque<Integer> ma = new MaxArrayDeque<>(new DefaultComparator());
        ArrayList<Integer> ads = new ArrayList<>();

        StringBuilder message = new StringBuilder();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                ma.addLast(randVal);
                ads.add(randVal);
                message.append("addLast(" + randVal + ")\n");
                assertEquals(message.toString(), ma.max(), Collections.max(ads));
            } else if (operationNumber == 1 && !ads.isEmpty()) {
                ma.removeFirst();
                ads.remove(0);
                if (!ads.isEmpty()) {
                    assertEquals(message.toString(), ma.max(), Collections.max(ads));
                }
            }
        }
    }
}
