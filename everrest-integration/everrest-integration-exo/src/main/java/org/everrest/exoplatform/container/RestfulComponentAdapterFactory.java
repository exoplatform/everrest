/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.exoplatform.container;

import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.ComponentAdapterFactory;

/**
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class RestfulComponentAdapterFactory implements ComponentAdapterFactory {
    private final ComponentAdapterFactory delegate;
    private final  RestfulContainer container;

    public RestfulComponentAdapterFactory(RestfulContainer container,ComponentAdapterFactory delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
        this.container = container;
    }

   @SuppressWarnings("rawtypes")
   @Override
   public <T> ComponentAdapter<T> createComponentAdapter(Object componentKey, Class<T> componentImplementation) throws ContainerException
   {
      if (RestfulComponentAdapter.isRestfulComponent(componentImplementation)) {
         return new RestfulComponentAdapter(container,componentKey, componentImplementation);
      }
      return delegate.createComponentAdapter(componentKey, componentImplementation);
   }
}
