package com.spring.batch.config;

import com.spring.batch.batch.reader.gdsTradeReader;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;

import java.lang.reflect.Field;
import java.util.Arrays;

@Configuration
@Slf4j
public class ReaderConfig {

    @Bean
    @StepScope
    @Qualifier("flatFileItemReader")
    public FlatFileItemReader<TradeDto> flatFileItemReader(LineMapper<TradeDto> lineMapper,
                                                           @Value("#{jobParameters['filePath']}") String inputFilePath,
                                                           FileMetadata fileMetadata) {
        FlatFileItemReader<TradeDto> reader = new FlatFileItemReader<>();
        try {
            FileSystemResource resource = new FileSystemResource(inputFilePath);
            if (!resource.exists()) {
                throw new IllegalStateException("File not Found: " + resource.getPath());
            }
            reader.setResource(resource);
            reader.setLinesToSkip(1);
            reader.setLineMapper(lineMapper);
            fileMetadata.setSourceSystem("GDS");
        } catch (IllegalStateException e) {
            log.error("File load error", e);
        }
        return reader;
    }

    @Bean
    public LineMapper<TradeDto> lineMapper() {
        DefaultLineMapper<TradeDto> lineMapper = new DefaultLineMapper<>();
        String[] fieldNames = getPoJoFieldNames(TradeDto.class);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames(fieldNames);
        tokenizer.setStrict(true);

        BeanWrapperFieldSetMapper<TradeDto> fieldMapper = new BeanWrapperFieldSetMapper<>();
        fieldMapper.setTargetType(TradeDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            if (fieldSet.getFieldCount() != fieldNames.length) {
                log.error("Invalid field count: expected {}, got {}", fieldNames.length, fieldSet.getFieldCount());
                return null;
            }
            return fieldMapper.mapFieldSet(fieldSet);
        });

        return lineMapper;
    }

    private String[] getPoJoFieldNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);
    }

    // Register as "gdsTradeReader" bean
    @Bean(name = "gdsTradeReader")
    @StepScope
    public gdsTradeReader gdsTradeReader(
            @Qualifier("flatFileItemReader") FlatFileItemReader<TradeDto> delegate,
            FileMetadata fileMetadata,
            NcsFeedDataService tradeService,
            PrincipalRepo principalRepo,
            ClientRepo clientRepo
    ) throws Exception {
        return new gdsTradeReader(delegate, fileMetadata, tradeService, principalRepo, clientRepo);
    }
}
