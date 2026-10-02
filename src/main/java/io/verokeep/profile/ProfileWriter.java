package io.verokeep.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.nio.file.Path;

public class ProfileWriter {

    private final ObjectMapper mapper;

    public ProfileWriter() {
        YAMLFactory yamlFactory = YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();
        this.mapper = new ObjectMapper(yamlFactory);
    }

    public void write(Profile profile, Path output) throws IOException {
        mapper.writeValue(output.toFile(), profile);
    }
}
