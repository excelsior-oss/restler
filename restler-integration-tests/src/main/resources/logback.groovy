import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date [%5.5level] [%50.50logger{50}] - %msg%n"
    }
}
root(DEBUG, ["CONSOLE"])
logger("org.eclipse", WARN)
logger("org.springframework", WARN)
logger("org.hibernate", WARN)
