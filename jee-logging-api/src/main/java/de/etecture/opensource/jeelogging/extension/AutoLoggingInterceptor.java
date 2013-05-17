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

import de.etecture.opensource.jeelogging.api.Fine;
import de.etecture.opensource.jeelogging.api.Finer;
import de.etecture.opensource.jeelogging.api.Finest;
import de.etecture.opensource.jeelogging.api.Info;
import de.etecture.opensource.jeelogging.api.LogEvent;
import de.etecture.opensource.jeelogging.api.LogEvent.Severity;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 *
 * @author rhk
 */
@Interceptor
@AutoLogging
public class AutoLoggingInterceptor {

	@EJB
	LogFactory logFactory;

	@PostConstruct
	void logConstruction(InvocationContext ctx) throws Throwable {
		Throwable t = null;
		try {
			ctx.proceed();
		} catch (Throwable tt) {
			t = tt;
		}
		AutoLogging autoLogging = ctx.getTarget().getClass().getAnnotation(AutoLogging.class);
		if (!autoLogging.postConstruct().isEmpty()) {
			String source = autoLogging.logger();
			if (source.isEmpty()) {
				source = ctx.getTarget().getClass().getName();
			}
			logFactory.log(source, autoLogging.severity(), autoLogging.postConstruct(), t);
		}
		if (t != null) {
			throw t;
		}
	}

	@PreDestroy
	void logDestroying(InvocationContext ctx) throws Throwable {
		AutoLogging autoLogging = ctx.getTarget().getClass().getAnnotation(AutoLogging.class);
		if (!autoLogging.preDestroy().isEmpty()) {
			String source = autoLogging.logger();
			if (source.isEmpty()) {
				source = ctx.getTarget().getClass().getName();
			}
			logFactory.log(source, autoLogging.severity(), autoLogging.postConstruct(), null);
		}
		ctx.proceed();
	}

	@AroundInvoke
	Object logMethod(InvocationContext ctx) throws Throwable {
		before(ctx);
		try {
			Object result = ctx.proceed();
			after(ctx);
			return result;
		} catch(Throwable t) {
			log(ctx,LogEvent.Severity.ERROR, ctx.getMethod().getName()+" failed!", t);
			throw t;
		}
	}

	private void before(InvocationContext ctx) {
		LogEvent.Severity severity = null;
		String message = "";
		if (ctx.getTarget().getClass().isAnnotationPresent(Info.class)) {
			severity = LogEvent.Severity.INFO;
			message = ctx.getTarget().getClass().getAnnotation(Info.class).before();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Fine.class)) {
			severity = LogEvent.Severity.FINE;
			message = ctx.getTarget().getClass().getAnnotation(Fine.class).before();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Finer.class)) {
			severity = LogEvent.Severity.FINER;
			message = ctx.getTarget().getClass().getAnnotation(Finer.class).before();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Finest.class)) {
			severity = LogEvent.Severity.FINEST;
			message = ctx.getTarget().getClass().getAnnotation(Finest.class).before();
		}
		if (message.isEmpty()) {
			message = ctx.getMethod().getName();
		}
		log(ctx, severity, message, null);
	}

	private void after(InvocationContext ctx) {
		LogEvent.Severity severity = null;
		String message = "";
		if (ctx.getTarget().getClass().isAnnotationPresent(Info.class)) {
			severity = LogEvent.Severity.INFO;
			message = ctx.getTarget().getClass().getAnnotation(Info.class).after();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Fine.class)) {
			severity = LogEvent.Severity.FINE;
			message = ctx.getTarget().getClass().getAnnotation(Fine.class).after();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Finer.class)) {
			severity = LogEvent.Severity.FINER;
			message = ctx.getTarget().getClass().getAnnotation(Finer.class).after();
		} else if (ctx.getTarget().getClass().isAnnotationPresent(Finest.class)) {
			severity = LogEvent.Severity.FINEST;
			message = ctx.getTarget().getClass().getAnnotation(Finest.class).after();
		}
		if (message.isEmpty()) {
			message = ctx.getMethod().getName()+" done.";
		}
		log(ctx, severity, message, null);
	}

	private void log(InvocationContext ctx, Severity severity, String message, Throwable t) {
		AutoLogging autoLogging = ctx.getTarget().getClass().getAnnotation(AutoLogging.class);
		String source = autoLogging.logger();
		if (source.isEmpty()) {
			source = ctx.getTarget().getClass().getName();
		}
		if (severity == null) {
			severity = autoLogging.severity();
		}
		logFactory.log(source, severity, message, t, ctx.getParameters());
	}
}
