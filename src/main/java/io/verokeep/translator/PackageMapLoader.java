package io.verokeep.translator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PackageMapLoader {

    public static final String DEFAULT_RESOURCE = "/package-map.yaml";

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public Map<String, Map<String, String>> loadDefault() {
        try (InputStream in = getClass().getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing bundled resource " + DEFAULT_RESOURCE);
            }
            return mapper.readValue(in, new TypeReference<LinkedHashMap<String, Map<String, String>>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + DEFAULT_RESOURCE, e);
        }
    }

    public Map<String, Map<String, String>> load(Path path) {
        try {
            return mapper.readValue(Files.readString(path), new TypeReference<LinkedHashMap<String, Map<String, String>>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + path, e);
        }
    }
}
