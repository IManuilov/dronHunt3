package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ScrambleString87 {


    public static void main(String[] args) {

        long tm =System.currentTimeMillis();
        System.out.println(
            new ScrambleString87().isScramble(
                //"keshachempion","ionempchashke"
                //"ab","aa"
                //"eebaacbcbcadaaedceaaacadccd","eadcaacabaddaceacbceaabeccd"
                //"cdeaadacb", "addaceacb"
                //"qqqqqw", "qqqqrq"
                //"xstjzkfpkggnhjzkpfjoguxvkbuopi", "xbouipkvxugojfpkzjhnggkpfkzjts"
                "abcdbdacbdac", "bdacabcdbdac"
            ));

        System.out.println(System.currentTimeMillis() - tm);
    }

    Map<String, Boolean> cache = new HashMap<>();

    public boolean isScramble(String s1, String s2) {
        if (!sameChars(s1, s2)) {
            return false;
        }

        return isScramble2(s1, s2);
    }

    public boolean isScramble2(String s1, String s2) {
        if (s1.equals(s2)) {
            return true;
        }

        if (s1.length() == 1) {
            return false;
        }

        if (s1.length() == 2) {
            return
                s1.charAt(0) == s2.charAt(1)
                    && s1.charAt(1) == s2.charAt(0);
        }


        String key = s1.length() > 1 ? s1 + s2 : null;

        Boolean prev = key != null ? cache.get(key) : null;

        if (prev != null) {
            return prev;
        }

        if (isScrambleInt(s1, s2)) {
            if (key != null)
                cache.put(key, true);
            return true;
        }

        String s1rev = new StringBuilder(s1).reverse().toString();
        if (isScrambleInt(s1rev, s2)) {
            if (key != null)
                cache.put(key, true);
            return true;
        }

        if (key != null)
            cache.put(key, false);
        return false;

    }

    public boolean isScrambleInt(String s1, String s2) {

        int sqInd = 0;

        char[] sq2 = s2.toCharArray();

        //int[] set = new int[s1.length()];
        int setsize = 0;
        int lastindex = -1;
        //
        while(sqInd < s1.length() - 1 ){
            char i1 = s1.charAt(sqInd++);
            int ind = findFirstChar(sq2, i1);

            if (ind < 0) {
                return false;
            }

            // удаляем
            sq2[ind] = (char)-1;
            // помечаем что присутвует
            //set[ind] = 1;
            lastindex = Math.max(lastindex, ind);
            setsize++;


            int delim = setsize;
            // слева и справа наборы совпадают
            if (setsize == lastindex + 1) {//set[setsize - 1] != 0) {
                String s1left = s1.substring(0, delim);
                String s2left = s2.substring(0, delim);

                String s1right = s1.substring(delim);
                String s2right = s2.substring(delim);

//                if (!sameChars(s1right, s2right)) {
//                    //System.out.println(s1left + "|" + s2left + "    " + s1right + " <> " + s2right + "     " );
//                    return false;
//                }

                if (isScramble2(s1left, s2left)
                    && isScramble2(s1right, s2right)) {
                    return true;
                }

            }
        }


        return false;
    }

    public boolean sameChars(String s1, String s2) {

        char[] a1 = s1.toCharArray();
        char[] a2 = s2.toCharArray();
        Arrays.sort(a1);
        Arrays.sort(a2);
        return Arrays.equals(a1, a2);
    }

    public static int findFirstChar(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i; // возвращаем позицию
            }
        }
        return -1; // символ не найден
    }
}
