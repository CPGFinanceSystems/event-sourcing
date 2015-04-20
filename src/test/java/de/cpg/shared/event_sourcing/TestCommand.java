package de.cpg.shared.event_sourcing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.shared.event_sourcing.command.Command;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestCommand.Builder.class)
public class TestCommand implements Command {
    private final String testData;


    @Override
    public String uniqueKey() {
        return testData;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {}
}
