package com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJsonSnapshot;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;

/**
 * Owner-local Hibernate UserType for ImportJobItem parsedPayload JSONB mapping,
 * ensuring high-precision BigDecimal numbers are preserved during entity loading.
 */
public class ImportPayloadJsonType implements UserType<Map<String, Object>> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Override
    public int getSqlType() {
        return SqlTypes.JSON;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Map<String, Object>> returnedClass() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public boolean equals(Map<String, Object> x, Map<String, Object> y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Map<String, Object> x) {
        return Objects.hashCode(x);
    }

    @Override
    public Map<String, Object> nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        String json = rs.getString(position);
        if (json == null || rs.wasNull()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON payload into Map", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Map<String, Object> value, int index, WrapperOptions options) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                String json = MAPPER.writeValueAsString(value);
                st.setObject(index, json, Types.OTHER);
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to serialize Map into JSON payload", e);
            }
        }
    }

    @Override
    public Map<String, Object> deepCopy(Map<String, Object> value) {
        return ImportJsonSnapshot.deepCopy(value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to disassemble Map", e);
        }
    }

    @Override
    public Map<String, Object> assemble(Serializable cached, Object owner) {
        if (cached == null) {
            return null;
        }
        try {
            return MAPPER.readValue((String) cached, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to assemble Map", e);
        }
    }
}
