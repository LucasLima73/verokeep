package io.verokeep.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProfileReader {

    private final ObjectMapper mapper;

    public ProfileReader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public Profile read(Path input) throws IOException {
        Profile profile = mapper.readValue(input.toFile(), Profile.class);
        return new Profile(
                profile.source(),
                orEmpty(profile.packages()),
                orEmpty(profile.flatpaks()),
                orEmpty(profile.dotfiles())
        );
    }

    private List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }
}
