package com.spring.batch.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.batch.model.FileMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TradeUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> prepareMetadata(FileMetadata fileMetadata) {
        List<String> kafkaMessages = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (String client : fileMetadata.getClients()) {
            try {
                ObjectNode rootNode = mapper.createObjectNode();
                ObjectNode metaDataNode = mapper.createObjectNode();
                metaDataNode.put("batchId", fileMetadata.getBatchIdForClient(client));
                metaDataNode.put("totalMessages", fileMetadata.getClientRecordCount(client));
                metaDataNode.put("SrcSys", "SRC");
                metaDataNode.put("fType", "TRADE");
                metaDataNode.put("mode", fileMetadata.getClientModes(client));
                metaDataNode.put("timestamp", Instant.now().toString());
                rootNode.set("metaData", metaDataNode);

                String messageJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
                kafkaMessages.add(messageJson);
            } catch (Exception e) {
                e.printStackTrace(); // Optionally log error for specific client
            }
        }

        return kafkaMessages;
    }

}