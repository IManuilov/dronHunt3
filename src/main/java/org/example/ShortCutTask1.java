package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ShortCutTask1 {

    public static void main(String[] args) {


        System.out.println(
            new ShortCutTask1().method(List.of("a1","a2","a1","a3", "a3"))
        );
    }

    Map<String, Integer> method(List<String> list) {


        Map<String, AtomicInteger> result = new HashMap<>();
        for (String s : list) {
            result.computeIfAbsent(s, (k) -> new AtomicInteger(0))
                .incrementAndGet();
        }

        return result.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                v->v.getValue().get()
            ));



    }
}
