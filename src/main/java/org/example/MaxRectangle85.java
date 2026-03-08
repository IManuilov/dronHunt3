package org.example;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class MaxRectangle85 {


    public static void main(String[] args) throws IOException {


        char[][] p = new char[][] {
//            {'1','0','1','0','0'},
//            {'1','0','1','1','1'},
//            {'1','1','1','1','1'},
//            {'1','0','0','1','0'}
//            {'0','0','1'},
//            {'1','1','1'}

//            {'1','0','1','1','0','1'},
//            {'1','1','1','1','1','1'},
//            {'0','1','1','0','1','1'},
//            {'1','1','1','0','1','0'},
//            {'0','1','1','1','1','1'},
//            {'1','1','0','1','1','1'}

//            {'1','1','1','1'},
//            {'1','1','1','1'},
//            {'1','1','1','1'}

//            {'1','1','0','1'},
//            {'1','1','0','1'},
//            {'1','1','1','1'}

            {'1','0','1','1','1'},
            {'0','1','0','1','0'},
            {'1','1','0','1','1'},
            {'1','1','0','1','1'},
            {'0','1','1','1','1'}
        };

//        p = readMatrixFromFile("C:\\work\\my\\j25_01\\src\\main\\resources\\maxrect.json");

        var inst = new MaxRectangle85();

        System.out.println(inst.maximalRectangle(p));

        System.out.println(inst.bestfx + " " + inst.bestfy + " - " + inst.bestcx + " " + inst.bestcy);
    }

    public static char[][] readMatrixFromFile(String filename) throws IOException {
        Gson gson = new Gson();

        // Читаем как массив массивов строк
        String[][] stringMatrix = gson.fromJson(new FileReader(filename), String[][].class);

        // Конвертируем в char[][]
        char[][] matrix = new char[stringMatrix.length][];

        for (int i = 0; i < stringMatrix.length; i++) {
            matrix[i] = new char[stringMatrix[i].length];
            for (int j = 0; j < stringMatrix[i].length; j++) {
                // Берем первый символ каждой строки
                matrix[i][j] = stringMatrix[i][j].charAt(0);
            }
        }

        return matrix;
    }



    int bestfx = 0;
    int bestfy = 0;
    int bestcx = 0;
    int bestcy = 0;

    public int maximalRectangle(char[][] matrix) {
        int maxSuare = 0;
        int[] result = new int[2];

        for (int y=0; y< matrix.length; y++) {

            if ((matrix[y].length) * (matrix.length - y) <= maxSuare) {
                break;
            }

            int lasty = 0;
            int lastx = 0;

            for (int x=0; x< matrix[y].length; x++) {

                if ((matrix[y].length - x) * (matrix.length - y) <= maxSuare) {
                    break;
                }
                int tsy;
                int tsx;

                if (x < lastx || matrix[y][x] == '1') {

                    int skip = (x >= bestfx && x < bestcx && y < bestcy) ? bestcy : 0;

                    //
                    tsy = getCy(matrix, x, y + skip) + skip;
                    if (tsy <= lasty) {
                        continue;
                    }

                    int skipx = (x >= bestfx && x < bestcx && y < bestcy) ? bestcx : 0;
                    tsx = getCx(matrix, x + skipx, y) + skipx;

                    int quiqs = (tsx - x) * (tsy - y);
                    if (quiqs <= maxSuare) {
                        continue;
                    }

                    int sq = crd(matrix, x, y, result);

                    if (sq >= maxSuare) {
                        maxSuare = sq;

                        lastx = result[0];
                        lasty = result[1];

                        bestfx = x;
                        bestfy = y;
                        bestcx = result[0];
                        bestcy = result[1];
                    }

                } else {
                    //lastx = 0;
                    lasty = 0;
                }

            }
            lasty = 0;
            //sx = 0;
        }

        return maxSuare;
    }


    public int crd(char[][] matrix, int x, int y, int[] result) {
        int maxsquare = 0;

//        if (x==3 && y==2) {
//            System.out.println();
//        }

        int cy = y;//(x >= bestfx && x < bestcx && y < bestcy) ? Math.max(bestcy-1, y) : y;
        int minx = matrix[y].length;

        while(cy < matrix.length && matrix[cy][x] == '1'){

            int cx = (x >= bestfx && x < bestcx && cy < bestcy)
                ? bestcx
                : x + 1;


            while(cx < minx && matrix[cy][cx] == '1'){
                cx++;
            }

            minx = cx;
            int sq = (cx - x) * (cy - y + 1);
            if (sq > maxsquare) {
                maxsquare = sq;
                result[0] = cx;
                result[1] = cy + 1;

            }

            cy++;
        }
        return maxsquare;
    }

    int getCx(char[][] matrix, int x, int y) {
        int minx = matrix[y].length;
        int cx = x;
        while(cx < minx && matrix[y][cx] == '1'){
            cx++;
        }

        return cx;
    }

    int getCy(char[][] matrix, int x, int y) {
        int minx = matrix.length;
        int cy = y;
        while(cy < minx && matrix[cy][x] == '1'){
            cy++;
        }

        return cy;

    }


}
