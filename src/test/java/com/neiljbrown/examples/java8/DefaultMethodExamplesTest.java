/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neiljbrown.examples.java8;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;

/**
 * Examples of the use of 'default' (aka 'virtual extension' or 'defender') methods in Java 8, implemented as a JUnit
 * test case.
 * 
 * <h2>What are Default Methods?</h2>
 * <p>
 * From Java 8 onwards, an interface can include default implementations for their method(s) which the compiler will use
 * in implementing classes that don't provide their own implementation.
 * <p>
 * Default methods were primarily added to the language to allow Java APIs to evolve (new methods to be added to
 * existing interfaces) without breaking backwards compatibility. This allowed many of the existing Collection APIs in
 * Java 8 to be retrofitted with new methods, including support for functional methods using the new Streams API, e.g.
 * {@link java.util.List#spliterator}, {@link java.util.Map#forEach}, {@link java.util.Iterator#forEachRemaining} and
 * other new methods, e.g. {@link java.util.Map#getOrDefault}.
 * <p>
 * Java interfaces are still NOT permitted to have state (due to the complexity that could arise when a class inherits
 * from multiple interfaces). Therefore default methods cannot alter a class' internal state directly, although they can
 * invoke other (both default and abstract) methods in the same (and possibly other) interface which could, as well as
 * static methods.
 * 
 * <h2>Multiple Inheritance edge cases</h2>
 * <p>
 * There are edge cases in which the introduction of default methods can cause compilation errors in an existing code
 * base (i.e. break backwards compatibility). This will only occur if a class implements two interfaces, both of which
 * have methods with default implementations that have <strong>identical</strong> signatures and the class itself
 * doesn’t provide an implementation. In this case the compiler doesn’t know which of the two default implementations to
 * use and the ambiguity will need to be resolved manually.
 * 
 * <h2>Default Methods vs. Abstract Classes</h2>
 * <p>
 * Java already has abstract classes which allow you to create an implementation of one or more interface methods for
 * reuse by concrete sub-classes. This raises the question of when you'd use a default method, rather than an abstract
 * class?
 * <p>
 * In addition to the primary aim of allowing new methods to be added to an interface without breaking backwards
 * compatibility, default methods are best suited to providing convenience or utility methods which previously would
 * have required a companion static utility class to have been created.
 * <p>
 * In contrast abstract classes remain a more powerful concept which support the inheritance of internal state as well
 * as behaviour, and the optional provision of a constructor. However, you can still only inherit from one abstract
 * class, whereas you can inherit default methods from multiple interfaces.
 * 
 * <h2>Further Reading</h2>
 * <p>
 * <a href="http://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html">Default Methods, Oracle Java
 * Tutorial</a>
 */
public class DefaultMethodExamplesTest {

  /**
   * A test case illustrating an example of an interface which provides default method(s), their inheritance and use by
   * one implementing class, and overriding the defaults in another implementing class.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testInheritAndOverrideDefaultMethod() throws Exception {
    // Create an instance of a (Logger) interface and utilise one of its default/extension methods
    Logger consoleLogger = new MyConsoleLoggerImpl();
    consoleLogger.info("Logged to console.");

    // Create an instance of the same interface which utilises an overridden implementation of the same default method
    final StringWriter stringWriter = new StringWriter();
    final String logMessage = "Logged to writer.";
    Logger myLogger = new MyLoggerImpl(stringWriter);
    myLogger.info(logMessage);
    assertThat(stringWriter.getBuffer().toString(), is("[INFO] " + logMessage));
  }

  /**
   * Example of an interface which declares a couple of methods with default implementations.
   */
  interface Logger {
    // The new 'default' keyword is used to denote a non-abstract method with a default implementation
    default void info(String message) {
      Logger.log("[INFO]", message);
    }

    // There can be more than one default method in an interface
    default void error(String message) {
      Logger.log("[ERROR]", message);
    }

    // Traditional declaration of an abstract method
    void warn(String message);

    // As of Java 8, interfaces can also include public static methods, which removes the need to create companion
    // static classes for utility methods. (Note that these methods can only be public).
    static void log(String level, String message) {
      System.out.println(level + " " + message);
    }
  }

  /**
   * Illustrates that a class can implement an interface without the need to provide its own implementation of an
   * interface method with a default implementation. This class relies on the default implementation of the
   * {@link Logger#info(String)} and {@link Logger#error(String)} methods.
   */
  class MyConsoleLoggerImpl implements Logger {
    @Override
    public void warn(String message) {
      System.out.println("[WARN] " + message);
    }
  }

  /**
   * Illustrates that a class can still override the default implementation of an interface method.
   */
  class MyLoggerImpl implements Logger {
    private Writer writer;

    public MyLoggerImpl(Writer writer) {
      this.writer = writer;
    }

    // Override default implementation in interface
    @Override
    public void info(String message) {
      try {
        this.writer.write("[INFO] " + message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Override default implementation in interface
    @Override
    public void error(String message) {
      try {
        this.writer.append("[ERROR] " + message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void warn(String message) {
      try {
        this.writer.append("[WARN] " + message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}