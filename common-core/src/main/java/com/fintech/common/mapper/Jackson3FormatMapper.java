package com.fintech.common.mapper;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;

/** This class is used as hibernate json configuration **/
public class Jackson3FormatMapper implements FormatMapper {

    private final JsonMapper jsonMapper;

    public Jackson3FormatMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }


    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return jsonMapper.readValue(charSequence.toString(),
                    jsonMapper.getTypeFactory().constructType(javaType.getJavaType()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON string", e);
        }
    }

    @Override
    public <T> String toString(T t, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return jsonMapper.writeValueAsString(t);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}

