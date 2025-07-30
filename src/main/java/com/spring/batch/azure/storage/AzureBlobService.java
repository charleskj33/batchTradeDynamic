package com.spring.batch.azure.storage;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class AzureBlobService {

    BlobServiceClient blobServiceClient;

    @Value("${azure.download-dir:${java.io.tmpdir}}")
    private String downloadDir;

    /*public AzureBlobService(@Value("${spring.azure.accountName}") String accountName){

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        String blobEnd = "http://ckj.blob.net";
        this.blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(blobEnd)
                .credential(credential)
                .buildClient();
        try{
            blobServiceClient.listBlobContainers().forEach(
                    container ->System.out.println(container.getName())
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public FileSystemResource downloadBlob(String containerName, String blobName) {
        BlobClient client = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        File outputFile = new File(downloadDir, blobName);

        try (BufferedOutputStream out =
                     new BufferedOutputStream(new FileOutputStream(outputFile))) {
            client.download(out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download blob", e);
        }

        return new FileSystemResource(outputFile);
    }*/

    public FileSystemResource downloadBlob(String containerName, String blobName) {
        return null;
    }
}
