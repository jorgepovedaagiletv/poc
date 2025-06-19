package org.java;

public class Main {

    public static void main(String[] args) throws Exception {

        // SCRIPT ON PYTHON WITH SENTENCE-BERT
        // USING SENTECE-BERT FOR CREATING EMBEDDINGS

        SenteceBert.createEmbeddingsWithSentenceBert();

        // PINECONE BBDD VECTORIAL
        // USING PINECONE TO CREATE INDEXES AND STORE EMBEDDINGS

        String pineconeKey = "pcsk_EoN5F_EP6pLoupzK3DYLZA15SNYxbGAc69FefiaM37nqtmd2jQnCtasiurfDWPj2U9UUi";
        String openaiKey = "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";
        String indexName = "hbo-taxonomies";
        String namespace = "default";

        PineConeBBDD.create2Index(pineconeKey, indexName);

        String text = "Qué es inteligencia artificial";
        float[] vector = PineConeBBDD.getEmbedding(openaiKey, text);

        PineConeBBDD.upsertVector(pineconeKey, indexName, namespace, "id1", vector);

        float[] queryVec = PineConeBBDD.getEmbedding(openaiKey, "Definición de IA");
        PineConeBBDD.queryVector(pineconeKey, indexName, namespace, queryVec);

        // AWS TITAN - creating embeddings

        AWSTitan.createEmbeddingsUsingTitanAWS();

        // AWS OpenSearch - CREATING INDEXES AND DATA WITH EMBEDDINGS VECTORS

        OpenSearchVectorsDB.Probar();
    }
}