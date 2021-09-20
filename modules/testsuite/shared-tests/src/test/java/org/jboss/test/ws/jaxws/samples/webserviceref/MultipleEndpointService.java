/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "EndpointService", targetNamespace = "http://org.jboss.ws/wsref", wsdlLocation = "META-INF/wsdl/MultipleEndpoint.wsdl")
public class MultipleEndpointService extends Service
{

   private final static URL ENDPOINTSERVICE_WSDL_LOCATION;
   private final static WebServiceException ENDPOINTSERVICE_EXCEPTION;
   private final static QName ENDPOINTSERVICE_QNAME = new QName("http://org.jboss.ws/wsref", "EndpointService");

   static
   {
      URL url = null;
      WebServiceException e = null;
      url = MultipleEndpointService.class.getResource("bogusAddress"); //invalid address on purpose, to test JBWS-3015 via service7 in EJB3Client
      if (url == null)
      {
         e = new WebServiceException("Cannot find wsdl, please put in classpath");
      }
      ENDPOINTSERVICE_WSDL_LOCATION = url;
      ENDPOINTSERVICE_EXCEPTION = e;
   }

   public MultipleEndpointService()
   {
      super(__getWsdlLocation(), ENDPOINTSERVICE_QNAME);
   }

   public MultipleEndpointService(URL wsdlLocation)
   {
      super(wsdlLocation, ENDPOINTSERVICE_QNAME);
   }

   public MultipleEndpointService(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public MultipleEndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features)
   {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * 
    * @return
    *     returns Endpoint
    */
   @WebEndpoint(name = "EndpointPort")
   public Endpoint getEndpointPort()
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort"), Endpoint.class);
   }

   @WebEndpoint(name = "EndpointPort2")
   public Endpoint getEndpointPort2()
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort2"), Endpoint.class);
   }

   /**
    * 
    * @param features
    *     A list of {@link jakarta.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
    * @return
    *     returns Endpoint
    */
   @WebEndpoint(name = "EndpointPort")
   public Endpoint getEndpointPort(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort"), Endpoint.class, features);
   }

   @WebEndpoint(name = "EndpointPort2")
   public Endpoint getEndpointPort2(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort2"), Endpoint.class, features);
   }

   private static URL __getWsdlLocation()
   {
      if (ENDPOINTSERVICE_EXCEPTION != null)
      {
         throw ENDPOINTSERVICE_EXCEPTION;
      }
      return ENDPOINTSERVICE_WSDL_LOCATION;
   }

}
