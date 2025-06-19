package org.java;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.CreateIndexForModelRequest;
import org.openapitools.db_control.client.model.CreateIndexForModelRequestEmbed;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_data.client.model.SearchRecordsRequestQuery;
import org.openapitools.db_data.client.model.SearchRecordsResponse;
import io.pinecone.proto.DescribeIndexStatsResponse;
import java.io.*;
import java.net.*;
import java.util.*;

import java.util.*;

public class PineConeBBDD {
    public static void main(String[] args) throws ApiException {
    }
    public static void createIndex(String tableName) throws ApiException {
        Pinecone pc = new Pinecone.Builder("pcsk_EoN5F_EP6pLoupzK3DYLZA15SNYxbGAc69FefiaM37nqtmd2jQnCtasiurfDWPj2U9UUi").build();

        String indexName = tableName;
        String region = "us-east-1";
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("text", "chunk_text");
        CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed()
                .model("llama-text-embed-v2")
                .fieldMap(fieldMap);

        pc.createIndexForModel(
                indexName,
                CreateIndexForModelRequest.CloudEnum.AWS,
                region,
                embed,
                DeletionProtection.DISABLED,
                null
        );
    }

    // CREAR LA TABLA
    public static void create2Index(String apiKey, String indexName) throws IOException {
        URL url = new URL("https://controller.us-west4-gcp-free.pinecone.io/databases");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Api-Key", "us-west4-gcp-free");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = String.format("""
        {
          "name": "%s",
          "dimension": 1536,
          "metric": "cosine"
        }
        """, indexName);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Create index response: " + responseCode);
    }

    // DEVUELVE LOS EMBEDDINGS
    public static float[] getEmbedding(String openaiApiKey, String text) throws IOException {
        URL url = new URL("https://api.openai.com/v1/embeddings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = String.format("""
        {
          "input": "%s",
          "model": "text-embedding-3-small"
        }
        """, text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.lines().reduce("", (acc, line) -> acc + line);
        in.close();

        // Extraer los embeddings (simplificado, puedes usar una librería JSON como Gson o Jackson)
        String vectorStr = response.split("\\[\\[")[1].split("]]")[0];
        String[] parts = vectorStr.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }

    // INSERTAR DATOS EN LA TABLA O INDICE
    public static void upsertVector(String apiKey, String indexName, String namespace, String id, float[] vector) throws IOException {
        String endpoint = indexName + "-your-project-id.svc.us-west4-gcp-free.pinecone.io"; // debes obtener tu project ID desde la consola
        URL url = new URL("https://" + endpoint + "/vectors/upsert");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Api-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        StringBuilder vectorString = new StringBuilder();
        for (float v : vector) {
            vectorString.append(v).append(",");
        }
        vectorString.setLength(vectorString.length() - 1); // quitar última coma

        String payload = String.format("""
        {
          "namespace": "%s",
          "vectors": [
            {
              "id": "%s",
              "values": [%s]
            }
          ]
        }
        """, namespace, id, vectorString.toString());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Upsert response: " + responseCode);
    }

    // BUSQUEDA SEMANTICA
    public static void queryVector(String apiKey, String indexName, String namespace, float[] queryVector) throws IOException {
        String endpoint = indexName + "-your-project-id.svc.us-west4-gcp-free.pinecone.io";
        URL url = new URL("https://" + endpoint + "/query");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Api-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        StringBuilder vectorString = new StringBuilder();
        for (float v : queryVector) {
            vectorString.append(v).append(",");
        }
        vectorString.setLength(vectorString.length() - 1);

        String payload = String.format("""
        {
          "namespace": "%s",
          "topK": 3,
          "includeMetadata": true,
          "vector": [%s]
        }
        """, namespace, vectorString.toString());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.lines().reduce("", (acc, line) -> acc + line);
        in.close();

        System.out.println("Query result: " + response);
    }


}