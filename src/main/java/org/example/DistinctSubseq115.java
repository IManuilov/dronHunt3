package org.example;

import java.util.HashMap;
import java.util.Map;

public class DistinctSubseq115 {

    public static void main(String[] args) {

        String [][] tst = {
            {"babgbag", "bag"},
            {"rabbbit", "rabbit"},
            {"adbdadeecadeadeccaeaabdabdbcdabddddabcaaadbabaaedeeddeaeebcdeabcaaaeeaeeabcddcebddebeebedaecccbdcbcedbdaeaedcdebeecdaaedaacadbdccabddaddacdddc",
                "bcddceeeebecbc"}
        };

        for (String[] t : tst) {
            long tm = System.currentTimeMillis();
            int res = new DistinctSubseq115().numDistinct(t[0], t[1]);

            System.out.println(t[0] + " " +  t[1] + " = " + res + "      " + (System.currentTimeMillis()-tm) + "ms "

            );
        }
    }

    String ps;
    String pt;
    char[] src;
    char[] dst;

    public int numDistinct(String s, String t) {
        ps = s;
        pt = t;
        src = s.toCharArray();// big
        dst = t.toCharArray();// small



        return ddd(0, 0);




    }

    Map<Integer, Integer> cache = new HashMap<>();
    int ddd(int dsti, int from) {
        char dstc = dst[dsti];

        int found = 0;
        for (int i = from; i < src.length-(dst.length - dsti - 1); i++) {
            char srcc = src[i];

            if (dstc == srcc) {
                if (dsti == dst.length-1) {
                    found++;
                } else {
                    Integer key = (dsti + 1) * 1024 + (i + 1);

                    int fndnext;
                    Integer val = cache.get(key);
                    if (val != null) {
                        fndnext = val;
                    } else {
                        // ищем следующий символ в src
                        fndnext = ddd(dsti + 1, i + 1);

                        cache.put(key, fndnext);
                    }

                    found += fndnext;
                }
            }
        }
        return found;
    }

}


