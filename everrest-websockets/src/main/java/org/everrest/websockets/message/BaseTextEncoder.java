/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.websockets.message;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @author andrew00x
 */
public abstract class BaseTextEncoder<T> implements Encoder.Text<T> {
    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
