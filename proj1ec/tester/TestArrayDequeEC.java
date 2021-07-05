package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();

        StringBuilder message = new StringBuilder();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                sad.addLast(randVal);
                ads.addLast(randVal);
                message.append("addLast(" + randVal + ")\n");
                assertEquals(message.toString(), sad.size(),ads.size());
            } else if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 100);
                sad.addFirst(randVal);
                ads.addFirst(randVal);
                message.append("addFirst(" + randVal + ")\n");
                assertEquals(message.toString(), sad.size(),ads.size());
            } else if (operationNumber == 2 && ads.size() > 0) {
                Integer s = sad.removeFirst();
                Integer a = ads.removeFirst();
                message.append("removeFirst()\n");
                assertEquals(message.toString(), s, a);
            } else if (ads.size() > 0){
                Integer s = sad.removeLast();
                Integer a = ads.removeLast();
                message.append("removeLast()\n");
                assertEquals(message.toString(), s, a);
            }
        }
    }
}
