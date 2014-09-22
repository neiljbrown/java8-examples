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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Examples of new methods added to {@link Iterable} interface in Java 8, and how they can be used to help process many
 * {@link java.util.Collection}, implemented as a JUnit test case.
 * <p>
 * Before J8, Iterable defined a single {@link Iterable#iterator()} method which supported the use of the for-each loop
 * statement (e.g. {@code for(T t : Iterable<T>)}. Two additional methods, with default implementations, have been added
 * in J8 - {@link Iterable#forEach(java.util.function.Consumer)} and {@link Iterable#spliterator()}.
 */
public class IterableTest {

  /**
   * Implementations of {@link Iterable} now support _internal_ iteration over a collection of elements, using
   * {@link Iterable#forEach(java.util.function.Consumer)}, a functional version of the external for-each loop.
   */
  @Test
  public void testForEach() {
    List<String> aList = new ArrayList<>();
    aList.add("foo");
    aList.add("bar");

    // Old way - external iteration using Iterable and for-each statement
    for (String s : aList) {
      assertNotNull(s);
    }

    // New way - internal iteration using Iterable
    // The Iterable interface provides a default implementation of forEach(Consumer action) which invokes
    // Consumer.accept() on each element. This example uses a lambda expression to implement the functional interface
    aList.forEach(s -> assertNotNull(s));
  }

  /**
   * The {@link java.util.Map} interface also defines a {@link java.util.Map#forEach(java.util.function.BiConsumer)}
   * method. This is similar to {@link Iterable#forEach(java.util.function.Consumer)} except the default implementation
   * passes the key/value pair of values of each {@link java.util.Map.Entry} to the functional interface.
   */
  @Test
  public void testMapInterfaceForEach() {
    Map<String, String> aMap = new HashMap<>();
    aMap.put("ooh", "aah");    
    aMap.put("foo", "bar");

    // Old way - external iteration using Iterable and for-each statement
    for (Map.Entry<String, String> mapEntry : aMap.entrySet()) {
      String key = mapEntry.getKey();
      assertNotNull(key);
      String value = mapEntry.getValue();
      assertNotNull(value);
    }

    // New way - internal iteration using Map.forEach()
    // This example uses a multi-line lambda expression to implement the BiConsumer functional interface
    aMap.forEach((k, v) -> {
      assertNotNull(k);
      assertNotNull(v);
    });
  }
}