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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Examples of the how the new {@link IntStream} class, an int primitive specialisation of a
 * {@link java.util.stream.Stream} can be used, e.g. as a functional alternative to the classic indexed for-loop,
 * implemented as a JUnit test case.
 * 
 * <h2>References</h2>
 * <p>
 * <a href="https://www.ibm.com/developerworks/library/j-java8idioms3/index.html">Java 8 idioms, Functional alternatives
 * to the traditional for loop, IBM developerWorks</a>
 */
public class IntStreamExamplesTest {

  /**
   * Provides an example of how the {@link IntStream#range(int, int)} method can be used in place of a classic for-loop
   * to sequentially iterate over a series of integers, incrementing them by one.
   * <p>
   * Unlike a for-loop {@link IntStream#range(int, int)} doesn't require intiailising a mutable index variable, or
   * specifying an increment operator - sequential iteration (by one) is implied.
   */
  @Test
  public void testRange() {
    // Classic index-based for-loop, with an initial inclusive start index & an exclusive end index, and an increment
    // operator, which in this case post increments, by one.
    int sum = 0;
    for (int i = 1; i < 6; i++) {
      sum += i;
    }
    final int expectedResult = 15;
    assertThat(sum).isEqualTo(expectedResult);

    final LongAdder a = new LongAdder();
    // Functional equivalent using the IntStream.range(start,end) method. The index parameter is received by the
    // Lambda function is an immutable parameter, which is effectively final.
    IntStream.range(1, 6).forEach(i -> a.add(i)); // or more simply a::add
    assertThat(a.intValue()).isEqualTo(expectedResult);
  }

  /**
   * Provides an example of the {@link IntStream#rangeClosed(int, int)} method. Differs to
   * {@link IntStream#range(int, int)} in that the upper bound of the iteration, specified in the second param, is
   * inclusive, rather than exclusive.
   */
  @Test
  public void testRangeClosed() {
    // Classic index-based for-loop, with an initial inclusive start index & an _inclusive_ end index, and an increment
    // operator, which in this case post increments, by one.
    int sum = 0;
    for (int i = 1; i <= 5; i++) {
      sum += i;
    }
    final int expectedResult = 15;
    assertThat(sum).isEqualTo(expectedResult);

    final LongAdder a = new LongAdder();
    // Functional equivalent using the IntStream.range(start,end) method.
    IntStream.rangeClosed(1, 5).forEach(a::add);
    assertThat(a.intValue()).isEqualTo(expectedResult);
  }

  /**
   * Provides an example of how to use the {@link IntStream#iterate(int, java.util.function.IntUnaryOperator)} method as
   * an alternative to a classic for-loop, when needing to iterate over integers and incrementing (or decrementing)
   * using a value other than one (as supported by IntStream.range()).
   * <p>
   * The {@link IntStream#iterate(int, java.util.function.IntUnaryOperator)} method returns an _infinite_ sequential
   * order stream of int, starting from a supplied initial value, with subsequent values (increments or decrements)
   * calculated by a supplied value. Because the stream is infinite a limit has to be applied to exit the iteration. As
   * of Java 8, the only way to do this is to apply a pre-calculated limit to the no. of iterations performed using
   * {@link IntStream#limit(long)}, an intermediate stream function. Having to calculate the exact no. of iterations in
   * advance is not ideal. In Java 9, new stream functions takeWhile(Predicate) and dropWhile(Predicate), will allow you
   * to supply a condition (predicate) e.g. {@literal "i <= 100"}.
   */
  @Test
  public void testIterateWhenSkippingValues() {
    // Classic index-based for-loop, with an initial inclusive start index & an exclusive end index, and an increment
    // operator, which in this case post increments, by two.
    int sum = 0;
    for (int i = 1; i < 100; i = i + 2) {
      sum += i;
    }
    final int expectedResult = 2500;
    assertThat(sum).isEqualTo(expectedResult);

    sum = IntStream.iterate(1, e -> e + 2) // Produce stream by applying a function which increments by 2
    .limit(50) // Stop the inifinite stream. In J8 the only way to do this is to apply a pre-calcu no. of iterations
    .sum();
    assertThat(sum).isEqualTo(expectedResult);
  }

  /**
   * Provides an example of how the {@link IntStream#iterate(int, java.util.function.IntUnaryOperator)} method can also
   * be used to iterate in reverse (count down).
   * <p>
   * The IntStream class' range() and rangeClosed() methods don't allow the start index to be greater than the end index
   * so they can't be used to iterate in reverse. The iterate() method has to be used instead.
   */
  @Test
  public void testIterateWhenIteratingInReverse() {
    // Classic index-based for-loop, with an initial inclusive start index & an exclusive end index, and a decrement
    // operator, which in this case decements by one
    int sum = 0;
    for (int i = 25; i > 0; i--) {
      sum += i;
    }
    final int expectedResult = 325;
    assertThat(sum).isEqualTo(expectedResult);

    sum = IntStream.iterate(25, e -> e - 1) // Produce stream by applying a function which decrements by 1
    .limit(25) // Stop the inifinite stream. In J8 the only way to do this is to apply a pre-calcu no. of iterations
    .sum();
    assertThat(sum).isEqualTo(expectedResult);

  }
}