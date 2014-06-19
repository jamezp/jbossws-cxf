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
package org.jboss.test.ws.jaxws.handlerauth;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test proper handler auth
 *
 * @author Alessio Soldano
 * @since 24-Sep-2013
 */
public class HandlerAuthTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-handlerauth2.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.handlerauth.LogicalSimpleHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpointImpl2.class)
               .addAsResource("org/jboss/test/ws/jaxws/handlerauth/handlers2.xml");
         }
      });
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-handlerauth.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SimpleHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/handlerauth/handlers.xml");
         }
      });
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-handlerauth3.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SecureEndpointImpl3.class)
               .addClass(org.jboss.test.ws.jaxws.handlerauth.SimpleHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/handlerauth/handlers.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/handlerauth/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   public static Test suite()
   {
      JBossWSTestSetup testSetup = new JBossWSTestSetup(HandlerAuthTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/handlerauth/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/handlerauth/jbossws-roles.properties").getAbsolutePath());
      testSetup.addSecurityDomainRequirement("handlerauth-security-domain", authenticationOptions);
      return testSetup;
   }
   
   public void testAuthSOAPHandler() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImplService"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpointPort"), SecureEndpoint.class);
      testAuth(port);
   }

   public void testAuthLogicalHandler() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth2?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImpl2Service"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpoint2Port"), SecureEndpoint.class);
      testAuth(port);
   }

   public void testNoHandlerAuth() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth3?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImpl3Service"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpoint3Port"), SecureEndpoint.class);
      setUser((BindingProvider)port, "John", "foo");
      int count = port.getHandlerCounter();
      int countOut = port.getHandlerCounterOutbound();
      int newCount;
      int newCountOut;
      
      assertEquals("Hello, Mr. John", port.sayHello("John"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      assertEquals("Bye, Mr. John", port.sayBye("John"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(++count, newCount); //verify count is increased
         assertEquals(++countOut, newCountOut); //verify countOut is increased
      }
      
      port.ping();
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(countOut, newCountOut); //verify countOut is not increased (oneway)
      
      assertEquals("foo", port.echo("foo"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      
      //Change user...
      setUser((BindingProvider)port, "Bob", "bar");
      
      assertEquals("Hello, Mr. Bob", port.sayHello("Bob"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      try {
         port.sayBye("Bob");
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(++count, newCount); //verify count is increased
         assertEquals(++countOut, newCountOut); //verify countOut is increased
      }
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(++count, newCount); //verify count is increased
         assertEquals(++countOut, newCountOut); //verify countOut is increased
      }
      
      assertEquals("foo2", port.echo("foo2"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
   }

   private void testAuth(final SecureEndpoint port) throws Exception
   {
      setUser((BindingProvider)port, "John", "foo");
      int count = port.getHandlerCounter();
      int countOut = port.getHandlerCounterOutbound();
      int newCount;
      int newCountOut;
      
      assertEquals("Hello, Mr. John", port.sayHello("John"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      assertEquals("Bye, Mr. John", port.sayBye("John"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(count, newCount); //verify count is *not* increased
         assertEquals(countOut, newCountOut); //verify countOut is *not* increased
      }
      
      port.ping();
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(countOut, newCountOut); //verify countOut is *not* increased (oneway)
      
      assertEquals("foo", port.echo("foo"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      
      //Change user...
      setUser((BindingProvider)port, "Bob", "bar");
      
      assertEquals("Hello, Mr. Bob", port.sayHello("Bob"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
      
      try {
         port.sayBye("Bob");
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(count, newCount); //verify count is *not* increased
         assertEquals(countOut, newCountOut); //verify countOut is *not* increased
      }
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(count, newCount); //verify count is *not* increased
         assertEquals(countOut, newCountOut); //verify countOut is *not* increased
      }
      
      try {
         port.ping();
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         newCountOut = port.getHandlerCounterOutbound();
         assertEquals(count, newCount); //verify count is *not* increased
         assertEquals(countOut, newCountOut); //verify countOut is *not* increased (it's oneway anyway)
      }
      
      assertEquals("foo2", port.echo("foo2"));
      newCount = port.getHandlerCounter();
      newCountOut = port.getHandlerCounterOutbound();
      assertEquals(++count, newCount);
      assertEquals(++countOut, newCountOut);
   }
   
   private static void setUser(BindingProvider bp, String username, String password) {
      bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
      bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
   }
}
