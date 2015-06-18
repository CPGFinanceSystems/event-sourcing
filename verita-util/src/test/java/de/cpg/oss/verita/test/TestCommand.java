package de.cpg.oss.verita.test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.verita.command.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestCommand.Builder.class)
public class TestCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final String uniqueKey;

    @Override
    public String uniqueKey() {
        return uniqueKey;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
