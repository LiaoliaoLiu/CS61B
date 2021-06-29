package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> N = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        SLList<Integer> testList = new SLList<>();
        int opPerCall = 1;
        int M = 10000;

        for (int i = 0; i < 8; ++i) {
            int count = (1 << i) * 1000;
            for (int j = 0; j < count; ++j) {
                testList.addLast(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < M; ++j) {
                testList.getLast();
            }
            times.addLast(sw.elapsedTime());
            N.addLast(count);
            opCounts.addLast(M);
        }

        printTimingTable(N, times, opCounts);
    }

}
