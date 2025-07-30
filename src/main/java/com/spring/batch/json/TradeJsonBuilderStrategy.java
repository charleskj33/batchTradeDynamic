package com.spring.batch.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.batch.model.TradeDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component("tradeJsonBuilderStrategy")
public class TradeJsonBuilderStrategy  implements JsonBuilderStrategy<TradeDto> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public String buildJson(TradeDto tradeDto){
        ObjectNode root = objectMapper.createObjectNode();
        addStringField(root, "principal", tradeDto.getPrincipal());
        addStringField(root, "counterParty", tradeDto.getCounterParty());
        addStringField(root, "tradeRef", tradeDto.getTradeRef());
        addJsonNode(root, "mtms", createMtmsArr(tradeDto));
        addJsonNode(root, "tradeDates", createTradeDates(tradeDto));
        addStringField(root, "valuationDate", tradeDto.getMtmValuationDate());

        ArrayNode customField = objectMapper.createArrayNode();
        addJsonNode(customField, createCustomFields("numericField1", tradeDto.getOrgCode()));
       /* addJsonNode(customField, createCustomFields("numericField2", tradeDto.getTradeRef2()));
        addJsonNode(customField, createCustomFields("field1", tradeDto.getMisc2()));
        addJsonNode(customField, createCustomFields("field2", tradeDto.getMisc4()));*/

        return convertJson(root);
    }

    private static JsonNode createCustomFields(String field, String value) {
        ObjectNode customFields = objectMapper.createObjectNode();
        if(field !=null && !field.isEmpty() && value!=null && !value.isEmpty()){
            addStringField(customFields, "field", field);
            if(field.contains("numericField")){
                addNumericField(customFields, "value", value);
            }else{
                addStringField(customFields, "value", value);
            }

        }
        return customFields;
    }


    private static JsonNode createTradeDates(TradeDto tradeDto) {
        ObjectNode tradeDates = objectMapper.createObjectNode();
       /* addStringField(tradeDates, "startDate", formatDate(tradeDto.getStartDate()));
        addStringField(tradeDates, "endDate", formatDate(tradeDto.getEndDate()));
        addStringField(tradeDates, "dealDate", formatDate(tradeDto.getDealDate()));*/
        return tradeDates;
    }

    private static String convertJson(ObjectNode node){
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        }catch (Exception e){
            throw new RuntimeException("Error Json", e);
        }
    }

    private static void addStringField(ObjectNode objectNode, String fieldName, String value ){
        if(value !=null && !value.isEmpty()){
            objectNode.put(fieldName, value);
        }
    }

    private static void addJsonNode(ObjectNode parent, String fieldName, JsonNode Child){
        if(Child !=null && !Child.isEmpty()){
            parent.set(fieldName, Child);
        }
    }

    private static void addJsonNode(ArrayNode parent, JsonNode Child){
        if(Child !=null && !Child.isEmpty()){
            parent.add(Child);
        }
    }

    private static ArrayNode createMtmsArr(TradeDto tradeDto){
        ArrayNode mtms = objectMapper.createArrayNode();
        addJsonNode(mtms, createMtms(tradeDto.getMtmValuation(), "USD"));

        return mtms.isEmpty() ? null : mtms;
    }

    private static ObjectNode createMtms(String mtmValuation, String ccy){
        ObjectNode mtms = objectMapper.createObjectNode();
        addNumericField(mtms, "amount", mtmValuation);
        addStringField(mtms, "currency", ccy);
        addStringField(mtms, "provider", "Principal");
        return mtms;
    }

    private static void addNumericField(ObjectNode objectNode, String fieldName, String value){
        if(value !=null){
            BigDecimal numericValue = formatNumber(value);
            try{
                if(numericValue !=null){
                    objectNode.put(fieldName, value);
                }
            }catch (NumberFormatException ignored){

            }
        }
    }

    private static BigDecimal formatNumber(String number){
        if(number == null || number.isBlank()){
            return null;
        }
        try{
            return new BigDecimal(number);
        }catch (NumberFormatException e){
            return  BigDecimal.ZERO;
        }
    }

    private static String formatDate(String dataStr){
        if(dataStr == null || dataStr.isEmpty()){
            return null;
        }
        try{
            LocalDate localDate = LocalDate.parse(dataStr, INPUT_FORMAT);
            return localDate.format(OUTPUT_FORMAT);
        }catch (DateTimeException e){
            throw new DateTimeException("Invalid excep "+ dataStr);
        }
    }
}
