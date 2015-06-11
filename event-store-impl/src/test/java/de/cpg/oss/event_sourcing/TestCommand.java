package de.cpg.oss.event_sourcing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.event_sourcing.command.Command;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestCommand.Builder.class)
public class TestCommand implements Command {
    private final String uniqueKey;


    @Override
    public String uniqueKey() {
        return uniqueKey;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {}
}
