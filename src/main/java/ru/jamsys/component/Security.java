package ru.jamsys.component;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Security {

    Map<String, String> map = new HashMap<>();

    public void add(String key, String value) {
        map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public void remove(String key) {
        map.remove(key);
    }

}
