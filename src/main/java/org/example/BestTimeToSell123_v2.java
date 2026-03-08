package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BestTimeToSell123_v2 {

    public static void main(String [] args) {

        int[] prices = {3,3,5,0,0,3,1,4};

        int res = new BestTimeToSell123_v2().maxProfit(prices);


        System.out.println(res);
    }

    int[] prices;

    public int maxProfit(int[] prices) {

        this.prices = prices;


        int min = prices[0];
        int min2 = prices[0];

        int minInd = 0;
        int minInd2 = 0;

        int grow = 0;
        int begin = 0;
        int grow2 = 0;
        int begin2 = 0;
        int end = 0;

        //List<Integer> res = new ArrayList<>();

        for (int i=0; i<prices.length; i++) {
            if (prices[i] < min) {
                min = prices[i];
                minInd = i;
            } else if (prices[i] < min2) {
                min2 = prices[i];
                minInd2 = i;
            }

            int f = prices[i] - min;
            if (f > grow) {
                if (begin != minInd) {
                    //res.add(grow);
                }
                grow = f;
                begin = minInd;
                end = i;

            }

            int f2 = prices[i] - min2;
            if (f2 > grow) {
                if (begin2 != minInd2) {
                    //res.add(grow);
                    //                    prevgrow = grow;
                    //                    prevbegin = begin;
                    //                    prevend = end;
                }
                grow2 = f2;
                begin2 = minInd2;
                end = i;

            }

        }

        return grow + grow2;

    }



}
