package marko.gdv.transform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class JoltSpecGenerator {

    public static void main(String[] args) {
        String inputFilePath = "input.json";
        String outputFilePath = "spec.json";

        try {
            // Đọc nội dung của input.json
            String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            JSONObject inputJson = new JSONObject(content);

            // Tạo JOLT Spec giữ nguyên cấu trúc của inputJson
            JSONArray specJson = new JSONArray();
            JSONObject operation = new JSONObject();
            operation.put("operation", "shift");
            JSONObject spec = new JSONObject();
            generateMapping("", inputJson, spec);
            operation.put("spec", spec);
            specJson.put(operation);

            // Ghi specJson vào spec.json
            try (FileWriter file = new FileWriter(outputFilePath)) {
                file.write(specJson.toString(4)); // Định dạng với thụt lề 4 spaces
                System.out.println("Successfully generated spec.json");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateMapping(String path, JSONObject inputJson, JSONObject spec) {
        Iterator<String> keys = inputJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = inputJson.get(key);
            String currentPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof JSONObject) {
                JSONObject childSpec = new JSONObject();
                generateMapping(currentPath, (JSONObject) value, childSpec);
                spec.put(key, childSpec);
            } else if (value instanceof JSONArray) {
                JSONObject arraySpec = new JSONObject();
                generateMapping(currentPath, (JSONArray) value, arraySpec);
                spec.put(key, arraySpec);
            } else {
                spec.put(key, currentPath);
            }
        }
    }

    private static void generateMapping(String path, JSONArray inputArray, JSONObject spec) {
        for (int i = 0; i < inputArray.length(); i++) {
            Object value = inputArray.get(i);
            String currentPath = path + ".*";

            if (value instanceof JSONObject) {
                JSONObject childSpec = new JSONObject();
                generateMapping(currentPath, (JSONObject) value, childSpec);
                spec.put("*", childSpec);
            } else if (value instanceof JSONArray) {
                JSONObject arraySpec = new JSONObject();
                generateMapping(currentPath, (JSONArray) value, arraySpec);
                spec.put("*", arraySpec);
            } else {
                spec.put("*", currentPath);
            }
        }
    }
}
