import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;

public class Outcast {

    private final WordNet data;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        data = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        ArrayList<String> validNouns = new ArrayList<>();
        for (String noun : nouns) {
            if (data.isNoun(noun)) {
                validNouns.add(noun);
            }
        }
        int farmost = 0;
        String result = "";
        for (int i = 0; i < validNouns.size(); ++i) {
            int distance = 0;
            for (int j = 0; j < validNouns.size(); ++j) {
                if (i != j) {
                    distance += data.distance(validNouns.get(i), validNouns.get(j));
                }
            }
            if (distance > farmost) {
                farmost = distance;
                result = validNouns.get(i);
            }
        }
        return result;
    }

    public static void main(String[] args)  {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}