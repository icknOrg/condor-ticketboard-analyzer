package org.coins1920.group05;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

public class TestUtils {

    public static final String APPLICATION_JSON = "application/json";

    public static <T> String readFromResourceFile(String fileName, Class<T> clazz) {
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

    public static <T> File getFileFromResourceFolder(String fileName, Class<T> clazz) throws FileNotFoundException {
        final URL resource = clazz
                .getClassLoader()
                .getResource(fileName);

        if (resource != null) {
            return new File(resource.getFile());
        } else {
            throw new FileNotFoundException("Could not find: " + fileName);
        }
    }
}
