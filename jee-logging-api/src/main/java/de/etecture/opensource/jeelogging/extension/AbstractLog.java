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
import de.etecture.opensource.jeelogging.api.LogEvent.Severity;
import java.io.Serializable;


public abstract class AbstractLog implements Log, Serializable {

    private static final long serialVersionUID = 1L;

	@Override
    public void debug(String message, Object... arguments) {
        debug(message, null, arguments);
    }

	@Override
    public void debug(String message, Throwable t, Object... arguments) {
        log(Severity.FINE, message, t, arguments);
    }

	@Override
    public void error(String message, Object... arguments) {
        error(message, null, arguments);
    }

	@Override
    public void error(String message, Throwable t, Object... arguments) {
        log(Severity.ERROR, message, t, arguments);
    }

	@Override
    public void info(String message, Object... arguments) {
        info(message, null, arguments);
    }

	@Override
    public void info(String message, Throwable t, Object... arguments) {
        log(Severity.INFO, message, t, arguments);
    }

	@Override
    public void log(Severity severity, String message, Object... arguments) {
        log(severity, message, null, arguments);
    }

	@Override
    public void warn(String message, Object... arguments) {
        warn(message, null, arguments);
    }

	@Override
    public void warn(String message, Throwable t, Object... arguments) {
        log(Severity.WARN, message, t, arguments);
    }
}
