package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ScrambleString87_v2 {


    public static void main(String[] args) {

        long tm =System.currentTimeMillis();
        System.out.println("result: " +
            new ScrambleString87_v2().isScramble(
                //"keshachempion","ionempchashke"
                //"ab","aa"
                "eebaacbcbcadaaedceaaacadccd","eadcaacabaddaceacbceaabeccd"
                //"cdeaadacb", "addaceacb"
                //"qqqqqw", "qqqqrq"
                //"xstjzkfpkggnhjzkpfjoguxvkbuopi", "xbouipkvxugojfpkzjhnggkpfkzjts"
                //"great","rgeat"
                //"abc","bca"
            ));

        System.out.println(System.currentTimeMillis() - tm);
    }

    Map<String, Boolean> cache = new HashMap<>();

    char[] cs1;
    char[] cs2;
    char[] csr1;
    //char[] csr2;

    public boolean isScramble(String s1, String s2) {
        cs1 = s1.toCharArray();
        cs2 = s2.toCharArray();
        csr1 = new StringBuilder(s1).reverse().toString().toCharArray();

        if (!sameChars(0, cs1.length)) {
           return false;
        }

        return isScramble2(0, s1.length(),0,false);
    }

    String margin(int deep) {
        String m = "";
        for (int i=0; i<deep; i++){
            m+="  ";
        }

        return m;
    }

    public boolean isScramble2(int b1, int e1, int b2, boolean reverse) {


//        System.out.println(margin(deep) + "s2:  " + b1 + " " + e1 + " " + b2 + " " + reverse);


        if (e1-b1 == 1) {
            char x1 = reverse
                ? csr1[cs1.length - (b1 + 1)]
                : cs1[b1];
            return  x1 == cs2[b2];
        }

        if (e1-b1 == 2) {
            char y1 = cs2[b2];
            char y2 = cs2[b2+1];

            char x1 = reverse
                ? csr1[cs1.length - (b1 + 1)]
                : cs1[b1];
            char x2 = reverse
                ? csr1[cs1.length - (b1 + 1 + 1)]
                : cs1[b1+1];

            return  x1 == y1 && x2 == y2 || x1 == y2 && x2 == y1;
        }


        //todo
        //String key = "";

        int begin = b1;
        int end = e1;
        char[] sss = cs1;
        if (reverse) {
            begin = cs1.length - e1;
            end = begin + (e1 - b1);
            sss = csr1;
        }

        String key = str(sss, begin, end) + str(cs2, b2, b2 + (e1 - b1));//s1 + s2;
        Boolean prev = cache.get(key);
//
        if (prev != null) {
            return prev;
        }

        if (isScrambleInt(b1, e1, b2, cs1, false)) {
            cache.put(key, true);
            return true;
        }


        if (isScrambleInt(b1, e1, b2, csr1, true)) {
            cache.put(key, true);
            return true;
        }

        cache.put(key, false);
        return false;

    }

    public boolean isScrambleInt(int b1, int e1, int b2, char[] s1, boolean reverse) {

        int begin = b1;
        int end = e1;

        if (reverse) {
            begin = cs1.length - e1;
            end = begin + (e1 - b1);
        }
//        System.out.println(margin(deep) + "scrint " + (reverse ? "REVERS" : "NORMAL") + "  "
//            + b1 + " " + e1 + " ("
//            + begin + " " + end + ")"
//            + " = " + s + " , " + b2 + " = " + str(cs2, b2, b2 + (e1-b1)));



        int sqInd = 0;

        int[] used = new int[e1 - b1];
        int setsize = 0;
        int lastindex = -1;
        //
        while(sqInd < (e1 - b1) - 1 ){
            char i1 = s1[begin + sqInd++];
            int ind = findFirstCharInS2(b2, b2 + (e1 - b1), i1, used);

            if (ind < 0) {
                return false;
            }

            setsize++;
            lastindex = Math.max(lastindex, ind);

            int delim = setsize;
            // слева и справа наборы совпадают
            if (setsize == lastindex + 1) {
                //String s1left = s1.substring(0, delim);
                //String s2left = s2.substring(0, delim);
                //String s1right = s1.substring(delim);
                //String s2right = s2.substring(delim);


                ///System.out.println(margin(deep) + "fnd similar: " + str(cs2, b2, b2+delim));

                // begin - begin+delim - end
                int p1 = begin;
                int p2 = begin + delim;
                int p3 = end;
                if (reverse) {
                    p1 = cs1.length - end;
                    p3 = cs1.length - begin;
                    p2 = p3 - delim;

                }


                if (   isScramble2( p1, p2, b2 + ( reverse ? p3-p2 : 0), reverse)
                    && isScramble2( p2, p3, b2 + (!reverse ? p2-p1 : 0), reverse)) {

                    return true;
                }
            }
        }

        return false;
    }

    public boolean sameChars(int b, int e) {

        char[] a1 = Arrays.copyOfRange(cs1, b, e);
        char[] a2 = Arrays.copyOfRange(cs2, b, e);
        Arrays.sort(a1);
        Arrays.sort(a2);
        return Arrays.equals(a1, a2);
    }

    public int findFirstCharInS2(int b, int e, char find, int[] used) {
        for (int i = b; i < e; i++) {
            if (used[i-b] == 0
                && cs2[i] == find) {

                used[i-b] = 1;
                return i - b;
            }
        }
        return -1; // символ не найден
    }

    String str(char[] ar, int b, int e) {
        return String.copyValueOf(ar).substring(b, e);
    }
}
