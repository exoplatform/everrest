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
package org.everrest.core.impl.provider;

import org.everrest.core.provider.EntityProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: StreamOutputEntityProvider.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
@Provider
public class StreamOutputEntityProvider implements EntityProvider<StreamingOutput> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // input is not supported
        return false;
    }


    @Override
    public StreamingOutput readFrom(Class<StreamingOutput> type,
                                    Type genericType,
                                    Annotation[] annotations,
                                    MediaType mediaType,
                                    MultivaluedMap<String, String> httpHeaders,
                                    InputStream entityStream) throws IOException {
        // input is not supported
        throw new UnsupportedOperationException();
    }


    @Override
    public long getSize(StreamingOutput t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return StreamingOutput.class.isAssignableFrom(type);
    }


    @Override
    public void writeTo(StreamingOutput t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        t.write(entityStream);
    }
}
