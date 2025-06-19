package org.java;


import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.*;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.script.Script;
import org.opensearch.script.ScriptType;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;


import java.util.*;

public class OpenSearchVectorExample {

    public static void Probar() throws Exception {

        try {

            // Cliente de conexión
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                                    String.valueOf(new HttpHost("https://search-domainopensearchtest-aooeqogindn743sjmp3udrjwsi.eu-west-1.es.amazonaws.com", 443, "https")))
                            .setHttpClientConfigCallback(httpClientBuilder -> {
                                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                                ((BasicCredentialsProvider) credsProvider).setCredentials(AuthScope.ANY,
                                        new UsernamePasswordCredentials("jorgepovedaagiltvos", "Acceso009*")); // O usa SigV4 para IAM
                                return httpClientBuilder.setDefaultCredentialsProvider((org.apache.hc.client5.http.auth.CredentialsProvider) credsProvider);
                            })
            );

        // Paso 1: Crear índice con campo vectorial
        String indexName = "max-taxonomies-index";

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.source("""
            {
              "settings": {
                "index": {
                  "knn": true
                }
              },
              "mappings": {
                "properties": {
                  "index_name": { "type": "keyword" },
                  "content": { "type": "text" },
                  "embedding": {
                    "type": "knn_vector",
                    "dimension": 5
                  }
                }
              }
            }
            """, XContentType.JSON);

        if (!client.indices().exists(new org.opensearch.client.indices.GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
            System.out.println("Índice creado: ");
        } else {
            System.out.println("El índice ya existe.");
        }

        // Paso 2: Insertar documento con embedding
        Map<String, Object> document = new HashMap<>();
        document.put("index_name", indexName);
        document.put("content", "Ejemplo de texto para prueba vectorial.");
        document.put("embedding", Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

        IndexRequest indexRequest = new IndexRequest(indexName)
                .id("1")
                .source(document);

        client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("Documento insertado.");

        // Paso 3: Búsqueda por similitud de embedding
        SearchRequest searchRequest = new SearchRequest(indexName);

        Map<String, Object> params = new HashMap<>();
        params.put("field", "embedding");
        params.put("query_value", Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));
        params.put("space_type", "l2");

        Script script = new Script(ScriptType.INLINE, "knn", "knn_score", params);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(3);
        sourceBuilder.query(QueryBuilders.scriptScoreQuery(QueryBuilders.matchAllQuery(), script));

        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("Resultados:");
        Arrays.stream(response.getHits().getHits()).forEach(hit -> {
            System.out.println(hit.getSourceAsString());
        });

        // Cierre
        client.close();

        } catch (Exception e) {
            String aux = e.getMessage();
            throw new RuntimeException(e);
        }
    }
}
