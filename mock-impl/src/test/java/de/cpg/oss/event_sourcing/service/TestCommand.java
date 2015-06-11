package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.command.Command;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class TestCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final String uniqueKey;

    @Override
    public String uniqueKey() {
        return uniqueKey;
    }
}
