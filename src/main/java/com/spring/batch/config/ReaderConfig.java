package com.spring.batch.config;

import com.spring.batch.batch.reader.GenericTradesReader;
import com.spring.batch.batch.reader.TradeReaderStrategy;
import com.spring.batch.model.CkjTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
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

    // ---------- Generic FlatFile Readers ----------

    @Bean
    @StepScope
    public FlatFileItemReader<TradeDto> gdsFlatFileReader(
            @Qualifier("gdsLineMapper") LineMapper<TradeDto> lineMapper,
            @Value("#{jobParameters['filePath']}") String filePath,
            FileMetadata fileMetadata
    ) {
        return buildReader(filePath, lineMapper, fileMetadata, "GDS");
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CkjTradeDto> ckjFlatFileReader(
            @Qualifier("ckjLineMapper") LineMapper<CkjTradeDto> lineMapper,
            @Value("#{jobParameters['filePath']}") String filePath,
            FileMetadata fileMetadata
    ) {
        return buildReader(filePath, lineMapper, fileMetadata, "CKJ");
    }

    private <T> FlatFileItemReader<T> buildReader(String filePath, LineMapper<T> lineMapper, FileMetadata fileMetadata, String sourceSystem) {
        FlatFileItemReader<T> reader = new FlatFileItemReader<>();
        FileSystemResource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("File not found at path: " + filePath);
        }

        reader.setResource(resource);
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper);
        fileMetadata.setSourceSystem(sourceSystem);
        return reader;
    }

    // ---------- Line Mappers ----------

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
        String[] fieldNames = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames(fieldNames);
        tokenizer.setStrict(true);

        BeanWrapperFieldSetMapper<T> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(clazz);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            if (fieldSet.getFieldCount() != fieldNames.length) {
                log.warn("Field count mismatch for class {}: expected {}, found {}",
                        clazz.getSimpleName(), fieldNames.length, fieldSet.getFieldCount());
                return null;
            }
            return mapper.mapFieldSet(fieldSet);
        });

        return lineMapper;
    }

    // ---------- Generic Strategy-based Readers ----------

    @Bean(name = "gdsTradeReader")
    @StepScope
    public GenericTradesReader<TradeDto> gdsTradeReader(
            FlatFileItemReader<TradeDto> gdsFlatFileReader,
            @Qualifier("gdsTradeReaderStrategy") TradeReaderStrategy<TradeDto> strategy,
            FileMetadata fileMetadata
    ) throws Exception {
        return new GenericTradesReader<>(gdsFlatFileReader, strategy, fileMetadata);
    }

    /*@Bean(name = "ckjTradeReader")
    @StepScope
    public GenericTradesReader<CkjTradeDto> ckjTradeReader(
            FlatFileItemReader<CkjTradeDto> ckjFlatFileReader,
            @Qualifier("ckjTradeReaderStrategy") TradeReaderStrategy<CkjTradeDto> strategy,
            FileMetadata fileMetadata
    ) throws Exception {
        return new GenericTradesReader<>(ckjFlatFileReader, strategy, fileMetadata);
    }*/
}

