package com.somamission.peanutbutter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

    static String getFileToJson(String fileName) {
        String content = "";
        String absolutePath = getResourceFilePath(fileName);
        try {
            content = new String(Files.readAllBytes(Paths.get(absolutePath)));
        } catch (IOException e) {
            logger.error("Error reading file");
        }

        return content;
    }

    private static String getResourceFilePath(String fileName) {
        File f = new File(Objects.requireNonNull(TestUtils.class.getClassLoader().getResource(fileName)).getFile());
        return f.getAbsolutePath();
    }
}
