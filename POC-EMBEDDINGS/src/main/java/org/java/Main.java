package org.java;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.*;
import java.net.URI;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        createEmbeddingsWithSentenceBert();

        PineConeBBDD.create2Index("us-west4-gcp-free","max-taxonomies");

        String pineconeKey = "pcsk_EoN5F_EP6pLoupzK3DYLZA15SNYxbGAc69FefiaM37nqtmd2jQnCtasiurfDWPj2U9UUi";
        String openaiKey = "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";
        String indexName = "hbo-taxonomies";
        String namespace = "default";

        PineConeBBDD.create2Index(pineconeKey, indexName);

        String text = "Qué es inteligencia artificial";
        float[] vector = createEmbeddingsWithSentenceBert(); //PineConeBBDD.getEmbedding(openaiKey, text);

        PineConeBBDD.upsertVector(pineconeKey, indexName, namespace, "id1", vector);

        float[] queryVec = PineConeBBDD.getEmbedding(openaiKey, "Definición de IA");
        PineConeBBDD.queryVector(pineconeKey, indexName, namespace, queryVec);

        //AWSTitan.createEmbeddingsUsingTitanAWS();
        OpenSearchVectorExample.Probar();

    }

    private static float[] createEmbeddingsWithSentenceBert() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("sentences", List.of("This is an example.", "This is another sentence."));
        String requestBody = mapper.writeValueAsString(requestMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);

        System.out.println("Embeddings: " + responseMap.get("embeddings"));

        return convertMapToFloatArray(responseMap);
    }

    public static float[] convertMapToFloatArray(Map<String, Object> map) {
        float[] result = new float[5];//[map.size()];
        int i = 0;

        for (Object value : map.values()) {
            try {
                if (value instanceof Number) {
                    result[i] = ((Number) value).floatValue();
                } else if (value instanceof String) {
                    result[i] = Float.parseFloat((String) value);
                } else {
                    throw new IllegalArgumentException("Valor no convertible a float: " + value);
                }
                i++;
            } catch (Exception e) {
                System.err.println("Error al convertir el valor: " + value + " - " + e.getMessage());
                result[i++] = 0.0f; // Opcional: valor por defecto en caso de error
            }
        }

        return result;
    }
}