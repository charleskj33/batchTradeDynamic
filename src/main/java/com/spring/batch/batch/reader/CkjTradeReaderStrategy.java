package com.spring.batch.batch.reader;

import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.model.CkjTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("ckjTradeReaderStrategy")
@RequiredArgsConstructor
public class CkjTradeReaderStrategy implements TradeReaderStrategy<CkjTradeDto> {

    private final NcsFeedDataService tradeService;

    @Override
    public List<CkjTradeDto> readAndPreprocess(FlatFileItemReader<CkjTradeDto> delegate,
                                               FileMetadata metadata) throws Exception {
        List<CkjTradeDto> trades = new ArrayList<>();
        delegate.open(new ExecutionContext());

        try {
            CkjTradeDto item;
            while ((item = delegate.read()) != null) {
                trades.add(item);
            }
        } catch (FlatFileParseException e) {
            log.error("CKJ file parse error: {}", e.getMessage());
        } finally {
            delegate.close();
        }

        List<CkjTradeDto> valid = validate(trades, metadata);
        metadata.setAllRecords(valid.stream().map(CkjTradeDto::getTradeRef).toList());
        return valid;
    }

    private List<CkjTradeDto> validate(List<CkjTradeDto> trades, FileMetadata metadata) {
        List<CkjTradeDto> valid = new ArrayList<>();
        for (CkjTradeDto trade : trades) {
            boolean hasMtm = isNotBlank(trade.getMtmValuation());
            boolean hasMtmDate = isNotBlank(trade.getMtmValuationDate());

            if (hasMtm && hasMtmDate) {
                valid.add(trade);
            } else {
                ExceptionEntity ex = new ExceptionEntity();
                ex.setBatchId(metadata.getBatchId());
                ex.setTradeRef(trade.getTradeRef());
                ex.setErrorMessage("Missing MTM or MTM Valuation Date in CKJ trade.");
                tradeService.addExcepAdd(ex);
            }
        }
        return valid;
    }

    private boolean isNotBlank(String val) {
        return val != null && !val.trim().isEmpty();
    }
}

