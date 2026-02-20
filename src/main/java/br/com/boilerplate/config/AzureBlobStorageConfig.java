package br.com.boilerplate.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobStorageConfig {

    @Value("${custom.azure.blob-storage.connection-string}")
    private String CONNECTION_STRING;

    @Value("${custom.azure.blob-storage.container-name}")
    private String CONTAINER_NAME;

    @Bean
    public BlobContainerClient blobContainerClient() {
        var client = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        client.createBlobContainerIfNotExists(CONTAINER_NAME);
        return client.getBlobContainerClient(CONTAINER_NAME);
    }
}
