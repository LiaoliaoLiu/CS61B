package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> l1 = new AListNoResizing<>();
        BuggyAList<Integer> l2 = new BuggyAList<>();

        for (int i = 4; i < 7; ++i) {
            l1.addLast(i);
            l2.addLast(i);
        }

        for (int i = 0; i < 3; ++i) {
            assertEquals(l1.removeLast(), l2.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> Lb = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                Lb.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeC = L.size();
                int sizeB = Lb.size();
                assertEquals(sizeC, sizeB);
            } else if (operationNumber == 2 && L.size() > 0) {
                int itemC = L.getLast();
                int itemB = Lb.getLast();
                assertEquals(itemC, itemB);
            } else if (L.size() > 0){
                int itemC = L.removeLast();
                int itemB = Lb.removeLast();
                assertEquals(itemC, itemB);
            }
        }
    }
}
