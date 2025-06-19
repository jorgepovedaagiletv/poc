package org.java;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;

public class AWSTitan {

    public static void createEmbeddingsUsingTitanAWS() {
        String text = "Hola, este es un ejemplo para generar embeddings con Titan de AWS.";

        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Input: texto para convertir en embedding
        String inputText = "Hola, ¿cómo estás?";

        // JSON de entrada según formato Titan Embeddings v2
        String requestBody = String.format("""
        {
            "inputText": "%s"
        }
        """, inputText);

        // Crear la solicitud
        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("amazon.titan-text-express-v1")
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
                .build();

        // Enviar solicitud
        InvokeModelResponse response = bedrockClient.invokeModel(request);

        // Obtener resultado
        String responseJson = response.body().asUtf8String();
        System.out.println("Respuesta del modelo Titan Embeddings:\n" + responseJson);
    }
}
