/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1283;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.Test;

import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1283] Attachment dropped on outbound messages if they have been added through a handler
 */
public class JBWS1283TestCase extends JBossWSTest
{
   private final String targetNS = "http://org.jboss.test.ws/jbws1283";
   private JBWS1283Endpoint port;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws1283.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1283.AttachmentHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283TestCase.VerifyAttachmentHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283TestCase.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws1283/jaxws-handlers-server.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1283TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1283Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1283/JBWS1283Service/JBWS1283EndpointImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(JBWS1283Endpoint.class);
   }

   public void testAttachmentResponse() throws Exception
   {
      // Add a client-side handler that verifes existence of the attachment
      BindingProvider bindingProvider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(new VerifyAttachmentHandler());
      bindingProvider.getBinding().setHandlerChain(handlerChain);

      port.requestAttachmentData();
   }

   // handler that verifies the attachment that have been added on the server-side
   static class VerifyAttachmentHandler extends GenericSOAPHandler<SOAPMessageContext>
   {
      @Override
      protected boolean handleInbound(SOAPMessageContext msgContext)
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         Iterator<?> it = soapMessage.getAttachments();
         while(it.hasNext())
         {
            try
            {
               AttachmentPart attachment = (AttachmentPart)it.next();
               System.out.println("Recv " + attachment.getContentType() + " attachment:");
               System.out.println("'"+attachment.getContent()+"'");
               return true;
            }
            catch (SOAPException e)
            {
               throw new RuntimeException("Failed to access attachment data");
            }
         }

         throw new IllegalStateException("Missing attachment on the client side");
      }
   }
}
