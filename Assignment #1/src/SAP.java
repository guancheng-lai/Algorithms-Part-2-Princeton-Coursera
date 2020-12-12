import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SAP {

    private final Digraph digraph;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }
        digraph = new Digraph(G);
    }

    private void firstBFS(int a, HashMap<Integer, Integer> ancestorA) {
        Queue<Integer[]> queue = new LinkedList<>();
        queue.add(new Integer[]{a, 0});
        ancestorA.put(a, 0);
        while (!queue.isEmpty()) {
            Integer[] current = queue.poll();
            for (int candidate : digraph.adj(current[0])) {
                int step = current[1] + 1;
                if (ancestorA.containsKey(candidate)) {
                    if (ancestorA.get(candidate) > step) {
                        ancestorA.put(candidate, step);
                    }
                    continue;
                }
                ancestorA.put(candidate, step);
                queue.add(new Integer[]{candidate, step});
            }
        }
    }

    // return {vertexId, distance}
    private int[] secondBFS(int b, HashMap<Integer, Integer> ancestorA, HashMap<Integer, Integer> visited) {
        Queue<Integer[]> queue = new LinkedList<>();
        queue.add(new Integer[]{b, 0});
        int[] res = new int[]{-1, Integer.MAX_VALUE};
        if (ancestorA.containsKey(b)) {
            res[0] = b;
            res[1] = ancestorA.get(b);
        }
        while (!queue.isEmpty()) {
            Integer[] current = queue.poll();
            for (int candidate : digraph.adj(current[0])) {
                if (visited.containsKey(candidate)) {
                    if (visited.get(candidate) > current[1] + 1) {
                        visited.put(candidate, current[1] + 1);
                    } else {
                        continue;
                    }
                }
                if (ancestorA.containsKey(candidate)) {
                    int distance = current[1] + 1 + ancestorA.get(candidate);
                    if (distance < res[1]) {
                        res[0] = candidate;
                        res[1] = distance;
                    }
                }
                visited.put(candidate, current[1] + 1);
                queue.add(new Integer[]{candidate, current[1] + 1});
            }
        }
        if (res[0] == -1) {
            res[1] = -1;
        }
        return res;
    }

    // return {sapIdx, distance(a, b, sap)}
    private int[] singleSAPHelper(int a, int b) {
        for (int candidate : digraph.adj(a)) {
            if (candidate == b) {
                return new int[]{b, 1};
            }
        }
        for (int candidate : digraph.adj(b)) {
            if (candidate == a) {
                return new int[]{a, 1};
            }
        }
        HashMap<Integer, Integer> ancestorA = new HashMap<>();
        HashMap<Integer, Integer> visited = new HashMap<>();
        firstBFS(a, ancestorA);
        int[] res = secondBFS(b, ancestorA, visited);
        return res;
    }

    private void validateVertex(int v) {
        if (v < 0 || digraph.V() <= v) {
            throw new IllegalArgumentException();
        }
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        if (v == w) {
            return 0;
        }
        return singleSAPHelper(v, w)[1];
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        if (v == w) {
            return v;
        }
        return singleSAPHelper(v, w)[0];
    }

    // return {sapIdx, distance(a, b, sap)}
    private int[] listSAPHelper(Iterable<Integer> v, Iterable<Integer> w) {
        HashMap<Integer, Integer> ancestorA = new HashMap<>();
        for (int a : v) {
            firstBFS(a, ancestorA);
        }
        int[] result = new int[]{-1, Integer.MAX_VALUE};
        HashMap<Integer, Integer> visited = new HashMap<>();
        for (int b : w) {
            int[] currentRes = secondBFS(b, ancestorA, visited);
            if (currentRes[0] != -1 && currentRes[1] < result[1]) {
                result[0] = currentRes[0];
                result[1] = currentRes[1];
            }
        }
        return result;
    }

    private int validateIterable(Iterable<Integer> it) {
        if (it == null) {
            throw new IllegalArgumentException();
        }
        int len = 0;
        for (Integer i : it) {
            if (i == null) {
                throw new IllegalArgumentException();
            }
            len++;
        }
        return len;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (validateIterable(v) == 0 || validateIterable(w) == 0) {
            return -1;
        }
        return listSAPHelper(v, w)[1];
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if(validateIterable(v) == 0 || validateIterable(w) == 0) {
            return -1;
        }
        return listSAPHelper(v, w)[0];
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
