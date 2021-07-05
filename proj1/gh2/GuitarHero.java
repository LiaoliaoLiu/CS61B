package gh2;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.introcs.StdAudio;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static final double CONCERT_A = 440.0;
    public static final double CONCERT_C = CONCERT_A * Math.pow(2, 3.0 / 12.0);
    private static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static GuitarString[] strings = new GuitarString[37];

    private GuitarHero() {
        for (int i = 0; i < strings.length; ++i) {
            strings[i] = new GuitarString(CONCERT_A * Math.pow(2.0, (double) (i - 24) / 12));
            System.out.println(CONCERT_A * Math.pow(2.0, (double) (i - 24) / 12));
        }
    }

    public static void main(String[] args) {
        new GuitarHero();
        char key;
        GuitarString s = new GuitarString(CONCERT_A); // Placeholder

        while (true) {
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                key = StdDraw.nextKeyTyped();
                if (keyboard.indexOf(key) >= 0) {
                    s = strings[keyboard.indexOf(key)];
                    s.pluck();
                }
            }

            /* If no key was pressed, long-lasting */
            StdAudio.play(s.sample());
            s.tic();
        }
    }
}

