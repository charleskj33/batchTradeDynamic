package com.spring.batch.batch.writer;

import com.spring.batch.entity.TradeEntity;
import com.spring.batch.json.JsonBuilderFactory;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.NcsFeedDataService;
import com.spring.batch.util.JsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("gdsTradeWriterStrategy")
@RequiredArgsConstructor
public class GdsTradeWriterStrategy implements TradeWriterStrategy<TradeDto> {

    private final JsonBuilderFactory jsonBuilderFactory;
    @Override
    public void write(List<TradeDtoWrapper<TradeDto>> items,
                      FileMetadata metadata,
                      NcsFeedDataService service) {

        List<TradeEntity> entities = new ArrayList<>();

        for (TradeDtoWrapper<TradeDto> wrapper : items) {
            try {
                TradeDto trade = wrapper.getTrade();
                String clientName = wrapper.getClientName();
                String externalId = wrapper.getExternalId();
                /*String json = JsonBuilder.buildJson(trade);*/
                String json = jsonBuilderFactory.getBuilder("GDS").buildJson(trade); // Dynamic selection

                String mode = service.determineMode(clientName);
                metadata.incrementClientRecord(clientName);
                metadata.setClientModes(clientName, "mode");
                prepareNcsFeedData(json, entities, clientName, externalId, metadata);
            } catch (Exception e) {
                log.error("Error writing trade for client {}: {}", wrapper.getClientName(), e.getMessage(), e);
            }
        }

       // service.persistMarketData(entities);
    }

    private void prepareNcsFeedData(String jsonOutput, List<TradeEntity> tradeEntityList, String clientName, String externalId, FileMetadata metadata){
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setBatchId(metadata.getBatchIdForClient(clientName));
        tradeEntity.setFeedType("trades");
        tradeEntity.setJsonOutput(jsonOutput);
        tradeEntity.setSourceSystem("gds");
        tradeEntity.setExternalId(externalId);
        tradeEntity.setInsertedTimeStamp(LocalDateTime.now());
        tradeEntity.setClientName(clientName);
        tradeEntityList.add(tradeEntity);
    }
}

