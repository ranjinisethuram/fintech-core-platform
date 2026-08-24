package com.fintech.orchestration.contextmapper;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class SagaContextMapper {

    private final ObjectMapper objectMapper;

    public SagaContextMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJson(String json, Class<T> clazz){
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new BaseException(CommonErrorCode.JSON_DESERIALIZATION_ERROR);
        }
    }

    public String toJson(Object sagaContext){
        try{
            return objectMapper.writeValueAsString(sagaContext);
        } catch (JacksonException e) {
            throw new BaseException(CommonErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }
}
