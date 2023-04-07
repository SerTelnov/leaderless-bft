package com.telnov.consensus.dbft.jsons;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperConfigure {

    public static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
