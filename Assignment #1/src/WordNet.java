import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class WordNet {

    private final HashMap<Integer, String> vertexIdx2Synset;
    private final HashMap<String, List<Integer>> string2VertId;
    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException();
        }
        vertexIdx2Synset = new HashMap<>();
        string2VertId = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(synsets))) {
            while (scanner.hasNextLine()) {
                String[] synset = scanner.nextLine().split(",");
                int v = Integer.parseInt(synset[0]);
                String[] synonyms = synset[1].split(" ");
                vertexIdx2Synset.put(v, synset[1]);
                for (String synonym : synonyms) {
                    if (string2VertId.get(synonym) != null) {
                        string2VertId.get(synonym).add(v);
                    } else {
                        List<Integer> valueList = new ArrayList<>();
                        valueList.add(v);
                        string2VertId.put(synonym, valueList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Digraph digraph = new Digraph(getNumberOfLines(hypernyms));
        try (Scanner scanner = new Scanner(new File(hypernyms))) {
            while (scanner.hasNextLine()) {
                String[] hypernym = scanner.nextLine().split(",");
                int currentNode = Integer.parseInt(hypernym[0]);
                for (int i = 1; i < hypernym.length; ++i) {
                    digraph.addEdge(currentNode, Integer.parseInt(hypernym[i]));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        sap = new SAP(digraph);
    }

    private int getNumberOfLines(String fileName) {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return string2VertId.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return string2VertId.containsKey(word);
    }

    private int[] helper(String nounA, String nounB) {
        int minDistance = Integer.MAX_VALUE;
        int ancestor = -1;
        for (int a : string2VertId.get(nounA)) {
            for (int b : string2VertId.get(nounB)) {
                int currRes = sap.length(a, b);
                if (currRes < minDistance) {
                    minDistance = currRes;
                    ancestor = sap.ancestor(a, b);
                }
            }
        }
        if (ancestor == -1) {
            return new int[]{-1, -1};
        }
        return new int[]{ancestor, minDistance};
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null || !isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        return helper(nounA, nounB)[1];
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null || !isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        int ancestor = helper(nounA, nounB)[0];
        return vertexIdx2Synset.get(ancestor);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        StdOut.println("INPUT: " + args[0] + " " + args[1]);
        WordNet wordNet = new WordNet(args[0], args[1]);
//        StdOut.println(wordNet.sap("Montmartre", "Forbidden_City"));
//        if (wordNet.isNoun("anamorphosis")) {
//            StdOut.println("anamorphosis");
//        }

        StdOut.println(wordNet.distance("microeconomic_expert","shirtfront"));
    }
}