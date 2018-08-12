package com.somamission.peanutbutter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.*;

public class TestUtils {

    public static String getFileToJson(String fileName) throws FileNotFoundException {
        Gson gson = new Gson();
        Reader reader = new FileReader(getResourceFilePath(fileName));
        JsonElement json = gson.fromJson(reader, JsonElement.class);
        return gson.toJson(json);
    }

    private static String getResourceFilePath(String fileName) {
        File f = new File(TestUtils.class.getClassLoader().getResource(fileName).getFile());
        return f.getAbsolutePath();
    }
}
