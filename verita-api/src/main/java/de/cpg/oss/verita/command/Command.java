package de.cpg.oss.verita.command;

import java.io.Serializable;

/**
 * Represents a persistent immutable command which is routed and persisted by the
 * {@link de.cpg.oss.verita.service.CommandBus} and * handled by an implemented {@link CommandHandler}.
 * A Command is something the user wants the system to do (like a <code>CreateUserCommand</code>). It can be accepted
 * or rejected by the system depending on the business rules.
 */
public interface Command extends Serializable {

    /**
     * Supply a unique logical identifier - for example for a <code>CreateUserCommand</code> this could be the user's
     * email address.
     *
     * @return A unique logical identifier for this command
     */
    String uniqueKey();
}
