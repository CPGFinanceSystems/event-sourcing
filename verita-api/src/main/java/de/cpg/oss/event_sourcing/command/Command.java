package de.cpg.oss.event_sourcing.command;

import java.io.Serializable;

public interface Command extends Serializable {

    String uniqueKey();
}
