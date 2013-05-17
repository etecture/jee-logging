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
package de.etecture.opensource.jeelogging.extension;

import de.etecture.opensource.jeelogging.api.Log;
import de.etecture.opensource.jeelogging.api.LogEvent;
import de.etecture.opensource.jeelogging.api.LogEvent.Severity;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

/**
 * this class is responsible to create an appropriate logger that will be
 * injected in some other classes
 *
 * @author rherschke
 */
@Singleton
public class LogFactory {

	@SuppressWarnings("AnnotationAsSuperInterface")
	private final static class WithSeverityBinding extends AnnotationLiteral<WithSeverity> implements WithSeverity {

		private static final long serialVersionUID = 1L;

		private final Severity value;

		public WithSeverityBinding(Severity value) {
			this.value = value;
		}

		@Override
		public Severity value() {
			return value;
		}
	}

	@SuppressWarnings("AnnotationAsSuperInterface")
	private final static class WithSourceBinding extends AnnotationLiteral<WithSource> implements WithSource {

		private static final long serialVersionUID = 1L;

		private final String value;

		public WithSourceBinding(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}
	}

	@SuppressWarnings("AnnotationAsSuperInterface")
	private final static class WithCauseBinding extends AnnotationLiteral<WithCause> implements WithCause {

		private static final long serialVersionUID = 1L;

		private final Class<? extends Throwable> value;

		public WithCauseBinding(Class<? extends Throwable> value) {
			this.value = value;
		}

		@Override
		public Class<? extends Throwable> value() {
			return value;
		}
	}
	@Inject
	private Event<LogEvent> events;

	@Produces
	public Log createLog(InjectionPoint injectionPoint) {
		final String source = injectionPoint.getMember().getDeclaringClass().getName();
		return new AbstractLog() {
			@Override
			public void log(Severity severity, String message, Throwable t, Object... arguments) {
				LogFactory.this.log(source, severity, message, t, arguments);
			}
		};
	}

	public void log(String source, Severity severity, String message, Throwable t, Object... arguments) {
		LogEvent event = new SimpleLogEvent(source, severity, message, t, arguments);
		if (t != null) {
			events.select(
					new WithSeverityBinding(event.getSeverity()),
					new WithSourceBinding(event.getSource()),
					new WithCauseBinding(event.getCause().getClass())).fire(event);
		} else {
			events.select(
					new WithSeverityBinding(event.getSeverity()),
					new WithSourceBinding(event.getSource())).fire(event);
		}
	}
}
