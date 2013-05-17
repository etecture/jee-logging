# Usages of this Module

## Integrating this module

This module based on an EJB jar, so it could be assembled in an EAR that forms the application.

In maven, an EAR module can be add an EJB type dependency:

	<dependency>
		<groupId>de.etecture.opensource</groupId>
		<artifactId>jee-logging-api</artifactId>
		<version>1.0.0</version>
		<type>ejb</type>
	</dependency>

This will add an entry in the application.xml of an EAR automatically. If not, an application assembler could add it manually:

	<?xml version="1.0" encoding="UTF-8"?>
	<application 
		xmlns="http://java.sun.com/xml/ns/javaee" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd"
		version="6">
		...
		<module>
			<ejb>jee-logging-api</ejb>
		</module>
		...
	</application>

Beside this, one of the following logging-bridges have to be added too:

**Delegating to JDK Logging facility**

	<dependency>
		<groupId>de.etecture.opensource</groupId>
		<artifactId>jee-logging-jul-bridge</artifactId>
		<version>1.0.0</version>
		<type>ejb</type>
	</dependency>

**Delegating to the Standard System Console**

	<dependency>
		<groupId>de.etecture.opensource</groupId>
		<artifactId>jee-logging-sysout-bridge</artifactId>
		<version>1.0.0</version>
		<type>ejb</type>
	</dependency>

Both bridges could be used together, but this would not make any sense.

## Using the Logging facility in a Session Bean

### Directly inject the log-facade

~~~~~
public class AnyService {
    @Inject
    Log log; // this is the logging facade
	
    public void anyServiceMethod() {
		log.info("Method %s just called", "anyServiceMethod");
        ...
    }
}
~~~~~

### Automatically log all method calls

~~~~~
@Autologging
public class AnyService {

    public void anyServiceMethod() {
        // start, stop and so on is logged automatically for this method
    }

    @Finer
    public void anotherServiceMethod() throws SomeException {
        // start and stop is logged with Severity FINER...
        ...
    }

    @Finer(before="method started", after="method done", failure="what's wrong here?")
    public void stillAnotherServiceMethod() {
        // start and stop logged with Severity FINER and with custom messages
    }
}
~~~~~

## Configuring the Log Levels

Log Levels are configured by the bridge itself. So refer to the bridge's documentation for details.

## Using Log Events inside the application

It is possible to observe LogEvents inside the application just by observing the CDI event:

~~~~~
public class MyOwnLoggingBridge {

    public void onLogEvent(@Observes LogEvent event) {
        // all Log-Events will be observed.
    }

    public void onErrorEvent(@Observes @WithSeverity(LogEvent.Severity.ERROR) LogEvent event) {
        // observes only Log-Events with severity ERROR
    }

    public void onFinerEventsFromMyService(
        @Observes 
        @WithSeverity(LogEvent.Severity.FINER)
        @WithSource(MyService.class.getName())
        LogEvent event) {
        // only Log-Events with severity FINER from MyService will be observed within this method
    }
}
~~~~~


