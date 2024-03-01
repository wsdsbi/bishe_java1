package test;

import java.util.*;
import java.util.stream.Collectors;

public class test {
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "John");
        map1.put("time", "260");
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "Alice");
        map2.put("time", "22");
        list.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", "Bob");
        map3.put("time", "30");
        list.add(map3);
        System.out.println(list);
        // Sort the list based on the "name" key in ascending order
        List<Map<String, Object>> sortedList = sortListOfMaps(list, "time");

        // Print the sorted list
        System.out.println(sortedList);
    }

    public static List<Map<String, Object>> sortListOfMaps(List<Map<String, Object>> list, String key) {
        return list.stream()
                .sorted(Comparator.comparing(map -> (String) map.get(key)))
                .collect(Collectors.toList());
    }
}
