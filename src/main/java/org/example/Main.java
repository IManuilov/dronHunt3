package org.example;

//import static java.lang.StringTemplate.STR;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

//public class Main {
//    public static void main(String[] args) {
//        System.out.println(
//"""
//Hello world!
//Я пришел к тебе с приветом
//""");
//
//
//        //System.out.println(STR."Value \{args[0]}");
//
//        Point p = new Point(1,2);
//
//        List<Future<Integer>> sum = new ArrayList<>();
//
//        // Типичный случай для виртуальных потоков
//        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//            IntStream.range(0, 10_000).forEach(i -> {
//                Future<Integer> f = executor.submit(() -> {
//                    Thread.sleep(Duration.ofSeconds(1)); // I/O симуляция
//
//
//                    return i;
//                });
//
//                sum.add(f);
//            });
//
//
//            sum.stream()
//                .forEach(f -> {
//                    try {
//                        System.out.println(f.get());
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//        } // 10к виртуальных потоков - эффективно
//
//
//
//        System.out.println(p.x() + "   " + p);
//    }
//}