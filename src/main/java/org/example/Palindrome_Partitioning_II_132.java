package org.example;

import org.junit.jupiter.api.Test;

public class Palindrome_Partitioning_II_132 {

    public static void main(String [] args) {


        int r = new Palindrome_Partitioning_II_132().minCut(
                //"cabababcbc"
                "aaabaa"
        );

        System.out.println(r);
    }


    char[] str;

    public int minCut(String s) {

        str = s.toCharArray();

        int count = 0;
        int first = 0;

        while(first < str.length) {
            count++;
            first = findPalindrom(first) + 1;
        }

        return count-1;
    }


    int findPalindrom(int first) {
        char c1 = str[first];
        //System.out.println(" " + first + "  " + c1);
        int last = str.length-1;


        again:
        while (last > first) {

            // ищем кандидата на конец палиндрома
            last = findBefore(c1, first, last);

            // check
            for (int j=1; j < (last - first + 1) / 2; j++) {

                //System.out.println("   " + str[first + j] + " <> " + str[last - j]);

                if (str[first + j] != str[last - j]) {

                    // не палиндром, уменьшаем
                    last--;
                    //System.out.println("  lst-- " + last);

                    continue again;
                }
            }
            return last;
        }

        return last;
    }

    private int findBefore(char c1, int first, int last) {
        for (int i = last; i >= first; i--) {
            if (str[i] == c1) {
                return i;
            }
        }


        return first;
    }


}
