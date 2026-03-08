package org.example;

public class Permutation60 {

    public static void main(String[] args) {
        new Permutation60().work();
    }

    void work() {
        for (int n=2; n<=8; n++) {
            int f = getGrp(n) * n;

            for (int k=1; k<= f; k++) {

                System.out.println(
                    n + "   " + k + "  > " +
                    getPermutation(n, k));
            }
            System.out.println("");

        }

    }

    public int getGrp(int n) {
        int gr1 = 1;
        for (int i = 2; i < n; i++) {
            gr1 *= i;
        }

        return gr1;
    }

    public int getDig(int n, int k) {
        int g = getGrp(n);
        int d = (k-1) / g;

        return d+1;
    }

    public String getPermutation(int n, int k) {


        int[] dig = new int[n];

        String digits = "";
        for(int i=0;i<n;i++){
            dig[i] = i+1;

            digits = digits + (i+1);
        }

        String res = "";
        for(int i=0; i<n; i++) {

            //int d = getDig(n-i, k) - 1;
            int g = getGrp(n-i);
            int d = (k - 1) / g;
            k = k - d * g;
            d = d + i;

            //

            res += digits.charAt(d);

            digits =
                digits.substring(0, i) +
                digits.charAt(d) +
                digits.substring(i, d) +
                digits.substring(d+1);
        }
        return res;
    }

}
/*
12
21

123
132
213
231
312
321


1234
1243
1324
1342
1423
1432
2134
2143
2314
2341
2413
2431
3



4

*/

