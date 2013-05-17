/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.jeelogging.bridges.jul;

import de.etecture.opensource.jeelogging.api.LogEvent;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * this is a simple logger that prints INFO and ERROR log events to the system
 * console
 *
 * @author rhk
 */
@Singleton
@Startup
@LocalBean
public class JDKLoggingBridge implements LoggingBridgeManagement {

    @Resource(lookup = "java:module/ModuleName")
    private String moduleName;
    @Resource(lookup = "java:app/AppName")
    private String appName;
    @Resource(name = "loglevel")
    private String logLevel = Level.FINE.getName();
    @Resource(name = "logFormat")
    private String logFormat = "%1$te.%1$tm.%1$tY - %1$tT [%2$s] %3$s";
    private boolean enabled = true;
    private MBeanServer platformMBeanServer = null;
    private ObjectName objectName = null;

    @PostConstruct
    public void registerInJMX() {
        Logger logger = getLogger();
        logger.setLevel(Level.parse(logLevel));
        logger.log(logger.getLevel(), String.format("setup the Java Util Logger for: %s with level: %s%n", logger.getName(), logLevel));
        try {
            objectName = new ObjectName("de.etecture.commons:type=" + this.getClass().getSimpleName());
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.registerMBean(this, objectName);
        } catch (Exception e) {
            throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
        }
    }

    @PreDestroy
    public void unregisterFromJMX() {
        try {
            if (platformMBeanServer != null) {
                platformMBeanServer.unregisterMBean(this.objectName);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onEvent(@Observes LogEvent logEvent) {
        if (!this.enabled) {
            return;
        }
        LogRecord lr = new LogRecord(Level.OFF, String.format(logFormat, logEvent.getTimestamp(), logEvent.getSource(), logEvent.getMessage()));

        switch (logEvent.getSeverity()) {
            case INFO:
                lr.setLevel(Level.INFO);
                break;
            case ERROR:
                lr.setLevel(Level.SEVERE);
                break;
            case FINE:
                lr.setLevel(Level.FINE);
                break;
            case FINER:
                lr.setLevel(Level.FINER);
                break;
            case FINEST:
                lr.setLevel(Level.FINEST);
                break;
            case WARN:
                lr.setLevel(Level.WARNING);
                break;
            case CONFIG:
                lr.setLevel(Level.CONFIG);
                break;
            default:
                throw new AssertionError(logEvent.getSeverity().name());
        }
        lr.setMillis(logEvent.getTimestamp().getTime());
        lr.setThrown(logEvent.getCause());
        getLogger().log(lr);
    }

    private Logger getLogger() {
        String loggerName;
        if (appName == null) {
            if (moduleName == null) {
                loggerName = Logger.GLOBAL_LOGGER_NAME;
            } else {
                loggerName = moduleName + ".logger";
            }
        } else {
            loggerName = appName + ".logger";
        }
        return Logger.getLogger(loggerName);
    }

    @Override
    public String getLogLevel() {
        return this.logLevel;
    }

    @Override
    public void setLogLevel(String level) {
        this.logLevel = level;
        Logger logger = getLogger();
        logger.setLevel(Level.parse(logLevel));
        logger.log(logger.getLevel(), String.format("changed the level for logger: %s to: %s%n", logger.getName(), logLevel));
    }

    @Override
    public String getLogFormat() {
        return this.logFormat;
    }

    @Override
    public void setLogFormat(String format) {
        this.logFormat = format;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public boolean getEnabled() {
        return this.enabled;
    }
}
