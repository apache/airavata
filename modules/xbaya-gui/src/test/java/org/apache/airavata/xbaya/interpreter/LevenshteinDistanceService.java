package org.apache.airavata.xbaya.interpreter;

/**
 * Created by IntelliJ IDEA.
 * User: heshan
 * Date: 12/18/11
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class LevenshteinDistanceService {
    public static int computeDistance(CharSequence sequence1, CharSequence sequence2) {
        int[][] distance = new int[sequence1.length() + 1][sequence2.length() + 1];

        for (int i = 0; i <= sequence1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= sequence2.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= sequence1.length(); i++) {
            for (int j = 1; j <= sequence2.length(); j++) {
                distance[i][j] = min(distance[i - 1][j] + 1, distance[i][j - 1] + 1, distance[i - 1][j - 1] + ((sequence1.charAt(i - 1) == sequence2.charAt(j - 1)) ? 0 : 1));
            }
        }

         //return "distance=" +distance[sequence1.length()][sequence2.length()];
         return distance[sequence1.length()][sequence2.length()];
    }

    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static void main(String[] args) {
        System.out.println(computeDistance("abcccdsfgsfdg", "fq"));
    }
}
