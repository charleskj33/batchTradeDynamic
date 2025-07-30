package com.spring.batch.config;

import com.spring.batch.azure.storage.AzureBlobService;
import com.spring.batch.batch.reader.GenericTradesReader;
import com.spring.batch.batch.reader.TradeReaderStrategy;
import com.spring.batch.model.CkjTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.lang.reflect.Field;
import java.util.Arrays;

@Configuration
@Slf4j
public class ReaderConfig {

    // ----------- FlatFileItemReaders -----------

    @Bean
    @StepScope
    public FlatFileItemReader<TradeDto> gdsFlatFileReader(
            @Qualifier("gdsLineMapper") LineMapper<TradeDto> lineMapper,
            AzureBlobService azureBlobService,
            @Value("#{jobParameters['containerName']}") String containerName,
            @Value("#{jobParameters['blobName']}") String blobName
    ) {
        return buildReader(azureBlobService, lineMapper, containerName, blobName);
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CkjTradeDto> ckjFlatFileReader(
            @Qualifier("ckjLineMapper") LineMapper<CkjTradeDto> lineMapper,
            AzureBlobService azureBlobService,
            @Value("#{jobParameters['containerName']}") String containerName,
            @Value("#{jobParameters['blobName']}") String blobName
    ) {
        return buildReader(azureBlobService, lineMapper, containerName, blobName);
    }

    private <T> FlatFileItemReader<T> buildReader(AzureBlobService azureBlobService,
                                                  LineMapper<T> lineMapper,
                                                  String containerName,
                                                  String blobName) {
        FlatFileItemReader<T> reader = new FlatFileItemReader<>();

        if (containerName == null || blobName == null) {
            throw new IllegalArgumentException("containerName and blobName must be provided as job parameters");
        }

        FileSystemResource resource = azureBlobService.downloadBlob(containerName, blobName);

        if (!resource.exists()) {
            throw new IllegalArgumentException("File not found after download: " + resource.getPath());
        }

        reader.setResource(resource);
        reader.setLinesToSkip(1); // Skip header line
        reader.setLineMapper(lineMapper);

        log.info("Configured FlatFileItemReader for blob '{}', container '{}'", blobName, containerName);
        return reader;
    }

    // ----------- Line Mappers -----------

    @Bean
    public LineMapper<TradeDto> gdsLineMapper() {
        return buildLineMapper(TradeDto.class);
    }

    @Bean
    public LineMapper<CkjTradeDto> ckjLineMapper() {
        return buildLineMapper(CkjTradeDto.class);
    }

    private <T> LineMapper<T> buildLineMapper(Class<T> clazz) {
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();

        // Get all field names in declared order
        String[] fieldNames = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(";");
        tokenizer.setNames(fieldNames);
        tokenizer.setStrict(true);

        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(clazz);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            if (fieldSet.getFieldCount() != fieldNames.length) {
                log.warn("Field count mismatch for class {}: expected {}, found {}",
                        clazz.getSimpleName(), fieldNames.length, fieldSet.getFieldCount());
                return null;  // or throw exception based on your fail-fast policy
            }
            return fieldSetMapper.mapFieldSet(fieldSet);
        });

        return lineMapper;
    }

    // ----------- Generic Strategy-based Readers -----------

    @Bean(name = "gdsTradeReader")
    @StepScope
    public GenericTradesReader<TradeDto> gdsTradeReader(
            FlatFileItemReader<TradeDto> gdsFlatFileReader,
            @Qualifier("gdsTradeReaderStrategy") TradeReaderStrategy<TradeDto> strategy,
            FileMetadata fileMetadata,
            NcsFeedDataService ncsFeedDataService
    ) throws Exception {
        return new GenericTradesReader<>(gdsFlatFileReader, strategy, fileMetadata,ncsFeedDataService);
    }

    /*@Bean(name = "ckjTradeReader")
    @StepScope
    public GenericTradesReader<CkjTradeDto> ckjTradeReader(
            FlatFileItemReader<CkjTradeDto> ckjFlatFileReader,
            @Qualifier("ckjTradeReaderStrategy") TradeReaderStrategy<CkjTradeDto> strategy,
            FileMetadata fileMetadata,
            NcsFeedDataService ncsFeedDataService
    ) throws Exception {
        return new GenericTradesReader<>(ckjFlatFileReader, strategy,fileMetadata, ncsFeedDataService);
    }*/
}
