package com.telnov.consensus.dbft.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.telnov.consensus.dbft.types.PeerNumber;
import com.telnov.consensus.dbft.types.PublicKey;

public class YamlObjectMapper {

    public static final ObjectMapper yamlObjectMapper = configYamlObjectMapper();

    private static ObjectMapper configYamlObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        final var module = new SimpleModule();
        module.addDeserializer(PublicKey.class, new PublicKeyDeserializer());
        module.addDeserializer(PeerNumber.class, new PeerNumberDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
