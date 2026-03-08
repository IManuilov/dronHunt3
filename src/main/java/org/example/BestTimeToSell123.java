package org.example;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BestTimeToSell123 {

    public static void main(String [] args) {

        int[] prices = {3,3,5,0,0,3,1,4};

        int res = new BestTimeToSell123().maxProfit(prices);


        System.out.println(res);
    }

    int[] prices;

    public int maxProfit(int[] prices) {

        this.prices = prices;

        //
        int[] grow1 = findBestGrowFromFall2(0, prices.length);
        int[] grow2 = {grow1[3], grow1[4], grow1[5]};//findBestGrowFromFall2(0, grow1[1]);
        int[] grow3 = findBestGrowFromFall2(grow1[2], prices.length);

        int f1 = findBestFall(grow1[1], grow1[2]);
        int f2 = findBestFall(grow2[1], grow2[2]);
        int f3 = findBestFall(grow3[1], grow3[2]);

        //pair
        int[] p = {grow1[0] + f1, grow2[0] + f2, grow3[0] + f3};
        Arrays.sort(p);
        int pairBest = p[2];

        int[] s = {grow1[0], grow2[0], grow3[0]};
        Arrays.sort(s);


        return Math.max(pairBest, s[1] + s[2]);

    }


    int findBestGrow(int from, int to) {
        int minVal = prices[from];
        int maxVal = prices[from];
        for (int i = from; i < to; i++) {
            if (prices[i] > maxVal) {
                maxVal = prices[i];
            }
            if (prices[i] < minVal) {
                minVal = prices[i];
            }
        }

        return maxVal-minVal;
    }

    int[] findBestGrowFromFall2(int from, int to) {

        int min = prices[from];
        int minInd = from;
        int grow = 0;
        int begin = from;
        int end = from;

        int prevgrow = 0;
        int prevbegin = from;
        int prevend = from;

        for (int i=from; i<to; i++) {
            if (prices[i] < min) {
                min = prices[i];
                minInd = i;
            }
            int f = prices[i] - min;
            if (f > grow) {
                if (begin != minInd) {
                    prevgrow = grow;
                    prevbegin = begin;
                    prevend = end;
                }
                grow = f;
                begin = minInd;
                end = i;

            }

        }
        return new int[] {grow, begin, end, prevgrow, prevbegin, prevend};
    }


    int findBestFall(int from, int to) {

        int max = prices[from];
        int fall = 0;

        for (int i=from + 1; i < to; i++) {
            int pr = prices[i];
            if (pr > max) {
                max = pr;
            }
            int f = max - pr;
            if (f > fall) {
                fall = f;
            }

        }
        return fall;
    }


}
