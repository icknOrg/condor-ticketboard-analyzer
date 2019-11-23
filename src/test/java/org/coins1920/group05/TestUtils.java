package org.coins1920.group05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TestUtils {

    public static final String APPLICATION_JSON = "application/json";

    public static String readFromResourceFile(String fileName, Class clazz) {
        try {
            try (InputStream inputStream = clazz
                    .getClassLoader()
                    .getResourceAsStream(fileName)) {
                if (inputStream == null) {
                    return null;
                } else {
                    final BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(inputStream)
                    );
                    return bufferedReader
                            .lines()
                            .collect(Collectors.joining());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
