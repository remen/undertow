package com.meltwater.owl.hello;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import net.logstash.logback.encoder.LogstashEncoder;

public class LogbackConfigurator extends ContextAwareBase implements Configurator {
    public void configure(LoggerContext lc) {
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
        ca.setContext(lc);
        ca.setName("console");

        LogstashEncoder logstashEncoder = new LogstashEncoder();
        logstashEncoder.start();

        ca.setEncoder(logstashEncoder);
        ca.start();

        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(ca);
    }
}
