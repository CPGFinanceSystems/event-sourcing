package de.cpg.oss.verita.service;

import de.cpg.oss.verita.command.CommandHandler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class CommandHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final CommandBus commandBus;

    public CommandHandlerRegistry(final CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        contextRefreshedEvent.getApplicationContext().getBeansOfType(CommandHandler.class).entrySet().stream()
                .forEach((entry) -> commandBus.subscribeTo(entry.getValue()));
    }
}
