package com.example.cab302project.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "deepseek-r1"; // You said you're using this one

    public static String askModel(String prompt) {
        try {
            // Create connection
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Create JSON payload
            String jsonInputString = String.format(
                    "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", MODEL, prompt.replace("\"", "\\\"")
            );

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8")
            )) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Extract only the `response` field from the JSON
            String fullResponse = response.toString();
            int start = fullResponse.indexOf("\"response\":\"") + 12;
            int end = fullResponse.indexOf("\"", start);
            if (start != -1 && end != -1 && end > start) {
                return fullResponse.substring(start, end).replace("\\n", "\n");
            }

            return "⚠️ Could not parse model response.";
        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Error communicating with Ollama server.";
        }
    }
}
