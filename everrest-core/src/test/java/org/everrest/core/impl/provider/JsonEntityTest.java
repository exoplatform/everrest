/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.provider;

import org.everrest.core.generated.Book;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonEntityTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class JsonEntityTest extends BaseTest
{

   @Path("/")
   public static class ResourceBook
   {
      @POST
      @Consumes("application/json")
      public void m1(Book book)
      {
         assertEquals("Hamlet", book.getTitle());
         assertEquals("William Shakespeare", book.getAuthor());
         assertTrue(book.isSendByPost());
      }
   }

   @Path("/")
   public static class ResourceBookArray
   {
      @POST
      @Consumes("application/json")
      public void m1(Book[] b)
      {
         assertEquals("Hamlet", b[0].getTitle());
         assertEquals("William Shakespeare", b[0].getAuthor());
         assertTrue(b[0].isSendByPost());
         assertEquals("Collected Stories", b[1].getTitle());
         assertEquals("Gabriel Garcia Marquez", b[1].getAuthor());
         assertTrue(b[1].isSendByPost());
      }
   }

   @Path("/")
   public static class ResourceBookCollection
   {
      @POST
      @Consumes("application/json")
      public void m1(List<Book> b)
      {
         assertEquals("Hamlet", b.get(0).getTitle());
         assertEquals("William Shakespeare", b.get(0).getAuthor());
         assertTrue(b.get(0).isSendByPost());
         assertEquals("Collected Stories", b.get(1).getTitle());
         assertEquals("Gabriel Garcia Marquez", b.get(1).getAuthor());
         assertTrue(b.get(1).isSendByPost());
      }
   }

   @Path("/")
   public static class ResourceBook2
   {
      @GET
      @Produces("application/json")
      public Book m1()
      {
         Book book = new Book();
         book.setTitle("Hamlet");
         book.setAuthor("William Shakespeare");
         book.setSendByPost(true);
         return book;
      }

      // Without @Produces annotation also should work.
      @POST
      public Book m2()
      {
         return m1();
      }
   }

   @Path("/")
   public static class ResourceBookArray2
   {
      @GET
      @Produces("application/json")
      public Book[] m1()
      {
         return createArray();
      }

      // Without @Produces annotation also should work.
      @POST
      public Book[] m2()
      {
         return createArray();
      }

      private Book[] createArray()
      {
         Book book1 = new Book();
         book1.setTitle("Hamlet");
         book1.setAuthor("William Shakespeare");
         book1.setSendByPost(true);
         Book book2 = new Book();
         book2.setTitle("Collected Stories");
         book2.setAuthor("Gabriel Garcia Marquez");
         book2.setSendByPost(true);
         return new Book[]{book1, book2};
      }
   }

   @Path("/")
   public static class ResourceBookCollection2
   {
      @GET
      @Produces("application/json")
      public List<Book> m1()
      {
         return createCollection();
      }

      // Without @Produces annotation also should work.
      @POST
      public List<Book> m2()
      {
         return createCollection();
      }

      private List<Book> createCollection()
      {
         Book book1 = new Book();
         book1.setTitle("Hamlet");
         book1.setAuthor("William Shakespeare");
         book1.setSendByPost(true);
         Book book2 = new Book();
         book2.setTitle("Collected Stories");
         book2.setAuthor("Gabriel Garcia Marquez");
         book2.setSendByPost(true);
         return Arrays.asList(book1, book2);
      }
   }

   private byte[] jsonBookData;

   private byte[] jsonArrayData;

   public void setUp() throws Exception
   {
      super.setUp();
      jsonBookData =
         ("{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true}").getBytes("UTF-8");
      jsonArrayData =
         ("[{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true},"
            + "{\"title\":\"Collected Stories\", \"author\":\"Gabriel Garcia Marquez\", \"sendByPost\":true}]")
            .getBytes("UTF-8");
   }

   public void testJsonEntityBean() throws Exception
   {
      ResourceBook r1 = new ResourceBook();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      // Object is transfered via JSON
      h.putSingle("content-type", "application/json");
      // with JSON transformation for Book have restriction can't pass BigDecimal
      // (has not simple constructor and it is not in JSON known types)
      h.putSingle("content-length", "" + jsonBookData.length);
      assertEquals(204, launcher.service("POST", "/", "", h, jsonBookData, null).getStatus());
      unregistry(r1);
   }

   public void testJsonEntityArray() throws Exception
   {
      ResourceBookArray r1 = new ResourceBookArray();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      // Object is transfered via JSON
      h.putSingle("content-type", "application/json");
      h.putSingle("content-length", "" + jsonArrayData.length);
      assertEquals(204, launcher.service("POST", "/", "", h, jsonArrayData, null).getStatus());
      unregistry(r1);
   }

   public void testJsonEntityCollection() throws Exception
   {
      ResourceBookCollection r1 = new ResourceBookCollection();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      // Object is transfered via JSON
      h.putSingle("content-type", "application/json");
      h.putSingle("content-length", "" + jsonArrayData.length);
      assertEquals(204, launcher.service("POST", "/", "", h, jsonArrayData, null).getStatus());
      unregistry(r1);
   }

   public void testJsonReturnBean() throws Exception
   {
      ResourceBook2 r2 = new ResourceBook2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("accept", "application/json");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      // ResourceBook2#m1()
      ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      Book book = ObjectBuilder.createObject(Book.class, parser.getJsonObject());
      assertEquals("Hamlet", book.getTitle());
      assertEquals("William Shakespeare", book.getAuthor());
      assertTrue(book.isSendByPost());

      // ResourceBook2#m2()
      writer.reset();
      response = launcher.service("POST", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      book = ObjectBuilder.createObject(Book.class, parser.getJsonObject());
      assertEquals("Hamlet", book.getTitle());
      assertEquals("William Shakespeare", book.getAuthor());
      assertTrue(book.isSendByPost());

      unregistry(r2);
   }

   public void testJsonReturnBeanArray() throws Exception
   {
      ResourceBookArray2 r2 = new ResourceBookArray2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("accept", "application/json");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      // ResourceBookArray2#m1()
      ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      Book[] book = (Book[])ObjectBuilder.createArray(new Book[0].getClass(), parser.getJsonObject());
      assertEquals("Hamlet", book[0].getTitle());
      assertEquals("William Shakespeare", book[0].getAuthor());
      assertTrue(book[0].isSendByPost());
      assertEquals("Collected Stories", book[1].getTitle());
      assertEquals("Gabriel Garcia Marquez", book[1].getAuthor());
      assertTrue(book[1].isSendByPost());
      //System.out.println("array: " + new String(writer.getBody()));

      // ResourceBookArray2#m2()
      writer.reset();
      response = launcher.service("POST", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      book = (Book[])ObjectBuilder.createArray(new Book[0].getClass(), parser.getJsonObject());
      assertEquals("Hamlet", book[0].getTitle());
      assertEquals("William Shakespeare", book[0].getAuthor());
      assertTrue(book[0].isSendByPost());
      assertEquals("Collected Stories", book[1].getTitle());
      assertEquals("Gabriel Garcia Marquez", book[1].getAuthor());
      assertTrue(book[1].isSendByPost());
      //System.out.println("array: " + new String(writer.getBody()));

      unregistry(r2);
   }

   public void testJsonReturnBeanCollection() throws Exception
   {
      ResourceBookCollection2 r2 = new ResourceBookCollection2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("accept", "application/json");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      // ResourceBookCollection2#m1()
      ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      ParameterizedType genericType = (ParameterizedType)new ArrayList<Book>(){}.getClass().getGenericSuperclass();
      //System.out.println(">>>>>"+genericType);
      List<Book> book = (List<Book>)ObjectBuilder.createCollection(List.class, genericType, parser.getJsonObject());
      assertEquals("Hamlet", book.get(0).getTitle());
      assertEquals("William Shakespeare", book.get(0).getAuthor());
      assertTrue(book.get(0).isSendByPost());
      assertEquals("Collected Stories", book.get(1).getTitle());
      assertEquals("Gabriel Garcia Marquez", book.get(1).getAuthor());
      assertTrue(book.get(1).isSendByPost());
      //System.out.println("collection: " + new String(writer.getBody()));

      // ResourceBookCollection2#m2()
      response = launcher.service("POST", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      book = (List<Book>)ObjectBuilder.createCollection(List.class, genericType, parser.getJsonObject());
      assertEquals("Hamlet", book.get(0).getTitle());
      assertEquals("William Shakespeare", book.get(0).getAuthor());
      assertTrue(book.get(0).isSendByPost());
      assertEquals("Collected Stories", book.get(1).getTitle());
      assertEquals("Gabriel Garcia Marquez", book.get(1).getAuthor());
      assertTrue(book.get(1).isSendByPost());
      //System.out.println("collection: " + new String(writer.getBody()));

      unregistry(r2);
   }
}
