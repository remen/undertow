package com.meltwater.owl.hello.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;
import net.logstash.logback.encoder.LogstashEncoder;

public class LogbackConfigurator extends ContextAwareBase implements Configurator {
    public void configure(LoggerContext lc) {
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
        ca.setContext(lc);
        ca.setName("console");

        Encoder<ILoggingEvent> encoder = buildEncoder(lc);
        ca.setEncoder(encoder);
        ca.start();

        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(ca);
    }

    private Encoder<ILoggingEvent> buildEncoder(LoggerContext lc) {
        if ("json".equals(System.getenv("LOG_FORMAT")) || "json".equals(System.getProperty("log.format"))) {
            return logstashEncoder();
        } else {
            return defaultEncoder(lc);
        }
    }

    private Encoder<ILoggingEvent> logstashEncoder() {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.start();
        return encoder;
    }

    private Encoder<ILoggingEvent> defaultEncoder(LoggerContext lc) {
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(lc);

        // same as
        // PatternLayout layout = new PatternLayout();
        // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        TTLLLayout layout = new TTLLLayout();

        layout.setContext(lc);
        layout.start();
        encoder.setLayout(layout);
        return encoder;
    }
}
