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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Examples of lambda expressions, implemented as a JUnit test case.
 * <p>
 * A lambda expression represents an anonymous (unnamed) function / method, which isn't associated with a class.
 * <p>
 * A lambda expression has a type of {@link java.lang.FunctionalInterface} when used in assignment statements or passed
 * as a method parameter.
 * <p>
 * The structure / syntax of a lambda expression is like that of a method, comprising: <br>
 * - Typed parameters (the left hand side of the {@code ->}). If the function takes no parameters then you use an empty
 * set of brackets: ().<br>
 * - A body (the right hand side of the {@code ->}) <br>
 * - A return type. For simple cases you can skip the return statement and the compiler will infer the return type.<br>
 * - Declaration of thrown exceptions.
 */
public class LambdaExpressionExamplesTest {

  /**
   * A lambda expression can be used to implement a single abstract method (SAM) interface, e.g. a {@link Comparator},
   * in a terser way than using an anonymous inner class.
   */
  @Test
  public void testInlineImplementationOfSingleAbstractMethod() {
    final List<String> sourceList = Arrays.asList("the", "quick", "brown", "fox", "jumped", "over");
    final List<String> expectedList = Arrays.asList("the", "fox", "over", "quick", "brown", "jumped");

    // Before J8, anonymous inner classes were the best way to implement a SAM, inline, e.g.
    final List<String> list1 = new ArrayList<>(sourceList);
    Collections.sort(list1, new Comparator<String>() {
      // Sorts two strings by their length
      @Override
      public int compare(String s1, String s2) {
        return s1.length() - s2.length();
      }
    });
    assertThat(list1, is(expectedList));

    // Lambda expressions now provide a terser way to implement a SAM inline
    // This removes the need for the class declaration, and the need to type the method signature.
    // Syntax is: (arg1, arg2) -> {Method body}.
    // Multi-line expressions must be surrounded with braces. Method return type is inferred by compiler
    Comparator<String> c = (String s1, String s2) -> s1.length() - s2.length();
    final List<String> list2 = new ArrayList<>(sourceList);
    Collections.sort(list2, c);
    assertThat(list2, is(expectedList));

    // In the example above the lambda expression is being used in an assignment, but it can also be used directly as a
    // method argument. See following examples.
  }

  /**
   * The java compiler can infer the types of method parameters in lambda expressions allowing the declaration to be
   * terser.
   */
  @Test
  public void testParameterTypeInference() {
    final List<String> sourceList = Arrays.asList("the", "quick", "brown", "fox", "jumped", "over");
    final List<String> expectedList = Arrays.asList("the", "fox", "over", "quick", "brown", "jumped");

    final List<String> list1 = new ArrayList<>(sourceList);
    Collections.sort(list1,
    // The type of the parameters can be inferred from the type of the list parameter in sort()
        (s1, s2) -> s1.length() - s2.length());
    assertThat(list1, is(expectedList));
  }

  /**
   * If you use a variable from the surrounding scope in an anonymous inner class that variable must be marked as final.
   * For lambda expressions this restriction has been loosened slightly so any variable accessed from the surrounding
   * scope must be "effectively final" - it doesn’t need to be explicitly declared as final but must behave as if it
   * were, i.e. it must have its value set only once.
   */
  @Test
  public void testVariableUsedFromSurroundingScopeEffectivelyFinal() {
    final List<String> sourceList = Arrays.asList("the", "quick", "brown", "fox", "jumped", "over");
    // Non-final variable used to filter the source list by string length
    int maxLength = 4;
    final List<String> expectedList = Arrays.asList("the", "fox", "over");

    // The lambda expression (of type java.util.function.Predicate), passed to java.util.Stream.filter(), is permitted
    // to refer to the non-final maxLength variable in surrounding scope
    List<String> list1 = sourceList.stream().filter(s -> s.length() <= maxLength).collect(Collectors.toList());

    assertThat(list1, is(expectedList));
  }

  /**
   * Lambda expressions can also be used as a reference to a method. In this case the body of the lambda expression is
   * simply a call to a method.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testMethodReference() throws Exception {
    // Use a lambda expression to create an implementation of the java.io.FileFilter functional interface
    FileFilter filter = (File f1) -> f1.canRead();
    // There is a special syntax for abbreviating a lambda which invokes a method on a single param of a given type -
    // “{class}::{method-name}” means invoke canRead() on the method param which is an instance of File
    filter = File::canRead;

    File f = Files.createTempFile(this.getClass().getCanonicalName(), ".tmp").toFile();
    f.deleteOnExit();
    File tempDir = f.getParentFile();
    File[] files = tempDir.listFiles(filter); // Apply the lambda
    assertThat(files.length, greaterThan(1));
  }
}