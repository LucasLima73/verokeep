package io.verokeep.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

public class ProfileReader {

    private final ObjectMapper mapper;

    public ProfileReader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public Profile read(Path input) throws IOException {
        return mapper.readValue(input.toFile(), Profile.class);
    }
}
