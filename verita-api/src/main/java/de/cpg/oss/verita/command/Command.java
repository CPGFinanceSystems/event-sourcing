package de.cpg.oss.verita.command;

import java.io.Serializable;

public interface Command extends Serializable {

    String uniqueKey();
}
