import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import java.awt.Color;
import java.io.File;
import java.util.PriorityQueue;
import java.util.Random;

public class SeamCarver {

    private class Coord {
        public int x, y;
        public Coord(int _x, int _y) {
            x = _x;
            y = _y;
        }
    }

    private class PQEntry implements Comparable<PQEntry> {
        public double e;
        public Coord curr, prev;
        public PQEntry(double _e, Coord _curr, Coord _prev) {
            e = _e;
            curr = _curr;
            prev = _prev;
        }

        @Override
        public int compareTo(PQEntry entry) {
            return Double.compare(e, entry.e);
        }
    }

    private class DPEntry {
        public double e;
        public Coord prev;
        public DPEntry(double _e, Coord _prev) {
            e = _e;
            prev = _prev;
        }
    }

    private Color[][] rgb;
    private double[][] energy;
    private boolean energyReady = false;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        int w = picture.width(), h = picture.height();
        if (w == 0 || h == 0) {
            throw new IllegalArgumentException();
        }
        rgb = new Color[w][h];
        for (int c = 0; c < w; ++c) {
            for (int r = 0; r < h; ++r) {
                rgb[c][r] = picture.get(c, r);
            }
        }
        energy = new double[w][h];
        calculateEnergy();
        energyReady = true;
    }

    // current picture
    public Picture picture() {
        if (width() == 0 || height() == 0) {
            return null;
        }
        Picture current = new Picture(rgb.length, rgb[0].length);
        for (int x = 0; x < rgb.length; ++x) {
            for (int y = 0; y < rgb[0].length; ++y) {
                current.set(x, y, rgb[x][y]);
            }
        }
        return current;
    }

    // width of current picture
    public int width() {
        return this.rgb.length;
    }

    // height of current picture
    public int height() {
        if (rgb.length == 0) {
            return 0;
        }
        return rgb[0].length;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            throw new IllegalArgumentException();
        }
        if (energy != null && energyReady) {
            return energy[x][y];
        }
        if (x == 0 || x == width()-1 || y == 0 || y == height()-1) {
            return 1000;
        }
        int deltaXRed = rgb[x+1][y].getRed() - rgb[x-1][y].getRed();
        int deltaXGreen = rgb[x+1][y].getGreen() - rgb[x-1][y].getGreen();
        int deltaXBlue =  rgb[x+1][y].getBlue() - rgb[x-1][y].getBlue();
        int deltaX = deltaXRed * deltaXRed + deltaXGreen * deltaXGreen + deltaXBlue * deltaXBlue;
        int deltaYRed = rgb[x][y+1].getRed() - rgb[x][y-1].getRed();
        int deltaYGreen = rgb[x][y+1].getGreen() - rgb[x][y-1].getGreen();
        int deltaYBlue = rgb[x][y+1].getBlue() - rgb[x][y-1].getBlue();
        int deltaY = deltaYRed * deltaYRed + deltaYGreen * deltaYGreen + deltaYBlue * deltaYBlue;
        return Math.sqrt(deltaX + deltaY);
    }

    private void calculateEnergy() {
        energy = new double[width()][height()];
        for (int x = 0; x < width(); ++x) {
            for (int y = 0; y < height(); ++y) {
                energy[x][y] = energy(x, y);
            }
        }
        energyReady = true;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (height() <= 2 || width() <= 2) {
            return new int[width()];
        }
        if (energy == null || !energyReady) {
            calculateEnergy();
        }
        DPEntry[][] dp = new DPEntry[width()][height()];
        for (DPEntry[] row : dp) {
            for (int i = 0; i < row.length; ++i) {
                row[i] = new DPEntry(Double.MAX_VALUE, null);
            }
        }
        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        for (int y = 1; y < height()-1; ++y) {
            pq.add(new PQEntry(energy(0, y) + energy(1, y), new Coord(1, y), new Coord(0, y)));
        }
        while (!pq.isEmpty()) {
            PQEntry pqEntry = pq.poll();
            if (dp[pqEntry.curr.x][pqEntry.curr.y].e > pqEntry.e) {
                dp[pqEntry.curr.x][pqEntry.curr.y].e = pqEntry.e;
                dp[pqEntry.curr.x][pqEntry.curr.y].prev = pqEntry.prev;
            } else {
                continue;
            }
            if (pqEntry.curr.x == width()-1) {
                continue;
            }
            if (pqEntry.curr.y > 1) {
                pq.add(new PQEntry(
                        energy(pqEntry.curr.x + 1, pqEntry.curr.y - 1) + pqEntry.e,
                        new Coord(pqEntry.curr.x + 1, pqEntry.curr.y - 1),
                        pqEntry.curr
                ));
            }
            pq.add(new PQEntry(
                    energy(pqEntry.curr.x + 1, pqEntry.curr.y) + pqEntry.e,
                    new Coord(pqEntry.curr.x + 1, pqEntry.curr.y),
                    pqEntry.curr
            ));
            if (pqEntry.curr.y < height()-2) {
                pq.add(new PQEntry(
                        energy(pqEntry.curr.x + 1, pqEntry.curr.y + 1) + pqEntry.e,
                        new Coord(pqEntry.curr.x + 1, pqEntry.curr.y + 1),
                        pqEntry.curr
                ));
            }
        }
        double minEnergy = Double.MAX_VALUE;
        int x = width() - 1, y = 1;
        for (int _y = 1; _y < height()-1; ++_y) {
            if (dp[x][_y].e < minEnergy) {
                minEnergy = dp[x][_y].e;
                y = _y;
            }
        }
        int[] res = new int[width()];
        res[x] = y;
        for (int col = x; col > 0; col--) {
            res[col-1] = dp[col][res[col]].prev.y;
        }
        return res;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        if (width() <= 2 || height() <= 2) {
            return new int[height()];
        }
        if (energy == null || !energyReady) {
            calculateEnergy();
        }
        DPEntry[][] dp = new DPEntry[width()][height()];
        for (DPEntry[] row : dp) {
            for (int i = 0; i < row.length; ++i) {
                row[i] = new DPEntry(Double.MAX_VALUE, null);
            }
        }
        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        for (int x = 1; x < width()-1; ++x) {
            pq.add(new PQEntry(energy(x, 0) + energy(x, 1), new Coord(x, 1), new Coord(x, 0)));
        }
        while (!pq.isEmpty()) {
            PQEntry pqEntry = pq.poll();
            if (dp[pqEntry.curr.x][pqEntry.curr.y].e > pqEntry.e) {
                dp[pqEntry.curr.x][pqEntry.curr.y].e = pqEntry.e;
                dp[pqEntry.curr.x][pqEntry.curr.y].prev = pqEntry.prev;
            } else {
                continue;
            }
            if (pqEntry.curr.y == height()-1) {
                continue;
            }
            if (pqEntry.curr.x > 1) {
                pq.add(new PQEntry(
                        energy(pqEntry.curr.x - 1, pqEntry.curr.y + 1) + pqEntry.e,
                        new Coord(pqEntry.curr.x - 1, pqEntry.curr.y + 1),
                        pqEntry.curr
                ));
            }
            pq.add(new PQEntry(
                    energy(pqEntry.curr.x, pqEntry.curr.y + 1) + pqEntry.e,
                    new Coord(pqEntry.curr.x, pqEntry.curr.y + 1),
                    pqEntry.curr
            ));
            if (pqEntry.curr.x < width()-2) {
                pq.add(new PQEntry(
                        energy(pqEntry.curr.x + 1, pqEntry.curr.y + 1) + pqEntry.e,
                        new Coord(pqEntry.curr.x + 1, pqEntry.curr.y + 1),
                        pqEntry.curr
                ));
            }
        }
        double minEnergy = Double.MAX_VALUE;
        int x = 1, y = height() - 1;
        for (int _x = 1; _x < width()-1; ++_x) {
            if (dp[_x][y].e < minEnergy) {
                minEnergy = dp[_x][y].e;
                x = _x;
            }
        }
        int[] res = new int[height()];
        res[y] = x;
        for (int row = y; row > 0; row--) {
            res[row-1] = dp[res[row]][row].prev.x;
        }
        return res;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null || rgb.length == 0 || rgb[0].length == 0 || seam.length != width()) {
            throw new IllegalArgumentException();
        }
        int prev = seam[0];
        for (int a : seam) {
            if (a < 0 || a >= height() || Math.abs(prev - a) > 1) {
                throw new IllegalArgumentException();
            }
            prev = a;
        }
        if (rgb[0].length == 1) {
            rgb = new Color[width()][0];
            return;
        }
        Color[][] result = new Color[width()][height()-1];
        for (int x = 0; x < seam.length; ++x) {
            System.arraycopy(rgb[x], 0, result[x], 0, seam[x]);
            System.arraycopy(rgb[x], seam[x] + 1, result[x], seam[x], height() - seam[x] - 1);
        }
        rgb = result;
        energy = null;
        energyReady = false;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null || rgb.length == 0 || rgb[0].length == 0 || seam.length != height()) {
            throw new IllegalArgumentException();
        }
        int prev = seam[0];
        for (int a : seam) {
            if (a < 0 || a >= width() || Math.abs(prev - a) > 1) {
                throw new IllegalArgumentException();
            }
            prev = a;
        }
        if (rgb.length == 1) {
            rgb = new Color[0][height()];
            return;
        }
        Color[][] result = new Color[width()-1][height()];
        for (int y = 0; y < height(); ++y) {
            boolean removed = false;
            for (int x = 0; x < width(); ++x) {
                if (seam[y] == x) {
                    removed = true;
                    continue;
                }
                if (removed) {
                    result[x-1][y] = rgb[x][y];
                } else {
                    result[x][y] = rgb[x][y];
                }
            }
        }
        rgb = result;
        energy = null;
        energyReady = false;
    }

    //  unit testing (optional)
    public static void main(String[] args) {
        double[][] pic = new double[5][5];
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                pic[x][y] = new Random().nextDouble() * 1000;
            }
        }
        Picture picture = new Picture(SCUtility.doubleToPicture(pic));
        SeamCarver sc = new SeamCarver(picture);
        int[] res = sc.findVerticalSeam();
        StdOut.print("V = ");
        for (int i : res) {
            StdOut.print(i);
        }
        StdOut.println("---------");
        sc.removeVerticalSeam(res);
        res = sc.findVerticalSeam();
        StdOut.print("V = ");
        for (int i : res) {
            StdOut.print(i);
        }
    }

}