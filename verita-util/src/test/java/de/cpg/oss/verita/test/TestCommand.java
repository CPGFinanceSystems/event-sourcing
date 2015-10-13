package de.cpg.oss.verita.test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.verita.command.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestCommand.Builder.class)
public class TestCommand extends Command {
    private static final long serialVersionUID = 1L;

    private final String uniqueKey;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }

    @Override
    public Optional<String> uniqueKey() {
        return Optional.of(uniqueKey);
    }
}
