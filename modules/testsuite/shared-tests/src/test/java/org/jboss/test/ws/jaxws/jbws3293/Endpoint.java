/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbws3293;

import java.util.concurrent.Future;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Response;

@WebService(name = "Endpoint", targetNamespace = "http://org.jboss.ws/jaxws/jbws3293")
@SOAPBinding(style = Style.RPC)
public interface Endpoint
{
   @WebMethod(operationName = "echo")
   public Response<String> echoAsync(@WebParam(name = "String_1") String string1);

   @WebMethod(operationName = "echo")
   public Future<?> echoAsync(@WebParam(name = "String_1") String string1, @WebParam(name = "asyncHandler") AsyncHandler<String> asyncHandler);

   @WebMethod
   @WebResult(name = "result")
   public String echo(@WebParam(name = "String_1") String string1);
}
