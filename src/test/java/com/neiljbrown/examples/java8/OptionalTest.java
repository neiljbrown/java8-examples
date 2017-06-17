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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.Random;

import org.junit.Test;

/**
 * Example usage patterns for the new {@link java.util.Optional} type, and its variants, available in Java 8,
 * implemented as a JUnit test case.
 * 
 * <h2>What is java.util.Optional?</h2>
 * <p>
 * java.util.Optional is a wrapper for a single value which may not be present. It's a type which explicitly conveys a
 * value might not be present and (the compiler) forces it to be handled in client code. The type also provides
 * convenience methods for dealing with the case where a value isn't present, avoiding the need for nested conditional
 * statements to handle null references.
 * <p>
 * You can also think of an Optional as a Collection of zero (empty) to at most one element. This way of thinking is
 * consistent with the fact that Optional supports similar methods to the Streams API.
 * 
 * <h2>What problem is Optional trying to solve?</h2>
 * <p>
 * java.util.Optional aims to reduce the no. of NullPointerException in code that are caused by developers not handling
 * the possibility that a method may return null. It allows more expressive APIs to be built which indicate that methods
 * may return null and force the null case to be handled.
 * 
 * <h2>How should Optional be used?</h2>
 * <p>
 * For certain classes, including business services, repositories and utility methods, {@code Optional<T>} should be
 * used as a method return type in preference to a null. The fact that Optional is not serialisable may make its use in
 * other classes, such as DTOs, unsuitable.
 * <p>
 * java.util.Optional is intended to be used primarily as a return type, and not as a method parameter, including
 * constructors.
 * <p>
 * Optional is also extensively used in functional programming (the Java 8 Streams API) as
 * {@link java.util.Optional#ifPresent} supports chaining functions which may not return values.
 * <p>
 * Summary usage guidelines:
 * <p>
 * Method return values – Consider using Optional instead of returning nulls as the intentions of the method API will 
 * clearer and calling code will be forced to handle the null case in a cleaner way.
 * <p>
 * Instance fields - Do not use Optional. It's not serializable and adds a wrapping overhead. Just use them within 
 * methods that process the fields instead.
 * <p>
 * Method params - Do not use Optional. It pollutes the method signature. Instead just promote params to Optional 
 * within the method if useful.
 * 
 * <h2>Further Reading</h2>
 * <ul>
 * <li><a href="http://java.dzone.com/articles/java-8-optional-how-use-it">Java 8 Optional: How to Use it</a></li>
 * <li><a href="http://www.oracle.com/technetwork/articles/java/java8-optional-2175753.html">Tired of Null Pointer
 * Exceptions? Consider Using Java SE 8's Optional!</a></li>
 * <li><a href="http://www.nurkiewicz.com/2013/08/optional-in-java-8-cheat-sheet.html">Optional in Java 8 cheat
 * sheet</a></li>
 * </ul>
 */
public class OptionalTest {

  /**
   * An Optional can be created for an empty value, a non-null value, or a null value.
   */
  @Test
  public void testCreate() {
    // Optional.empty() returns an Optional without a value
    Optional<String> o = Optional.empty();
    assertThat(o.isPresent(), is(false));

    // Optional.of() is used to create an Optional guaranteed to have a value (non-empty). Or, if the supplied value
    // is null, an NPE will be thrown
    final String s = "Hello World";
    o = Optional.of(s);
    assertThat(o.isPresent(), is(true));
    // Optional.of() throws NPE immediately if the supplied value is null, avoiding deferred NPE
    final String nullString = null;
    try {
      o = Optional.of(nullString);
      fail("Expected NullPointerException to be thrown.");
    } catch (NullPointerException expected) {
      // Expected
    }

    // Optional.ofNullable(), allows an Optional to be created which may or may not have a value (allows null)
    o = Optional.ofNullable(nullString);
  }

  /**
   * Optional provides methods, such as {@link Optional#ifPresent(java.util.function.Consumer)}, which allow you to more
   * easily deal with the presence or absence of a value, avoiding the need for conditional null checks. (Groovy
   * supports this use-case via its null-safe operator, {@code ?.}).
   */
  @Test
  public void testConditionallyProcessValueIfNotNull() {
    final String s = StringTestUtils.randomNullString("Hello");
    final StringBuilder sb = new StringBuilder();

    // Old way - When a value is returned that might be empty, a null check was required
    if (s != null) {
      sb.append(s);
    }

    // New way - Use Optional.ifPresent() to conditionally process the Optional if it has a value (is non-empty).
    // In this example, a lambda is used to implement the Consumer argument of ifPresent()
    Optional<String> optionalString = Optional.ofNullable(s); // e.g. what may be returned by a method
    optionalString.ifPresent(s2 -> sb.append(" World!")); //

    // Prove it worked as expected
    if (optionalString.isPresent()) {
      assertThat(sb.toString(), is("Hello World!"));
    } else {
      assertThat(sb.toString(), is(""));
    }
  }

  /**
   * Illustrates how to use {@link Optional#orElse(Object)} to handle the common pattern of using a default value if you
   * determine that the result of an operation is null. (Groovy supports this use-case via its Elvis operator,
   * {@code ?:}).
   */
  @Test
  public void testUseDefaultValueIfNull() {
    final String s = StringTestUtils.randomNullString("Hello");
    final StringBuilder sb = new StringBuilder();

    // Old way - Use ternary operator to provide default value if null
    final String defaultValue = "Goodbye";
    sb.append(s != null ? s : defaultValue);

    // New way - Use Optional.orElse() to use the value or provide default value if the Optional is empty
    Optional<String> optionalString = Optional.ofNullable(s); // e.g. what may be returned by a method
    sb.append(optionalString.orElse(defaultValue));

    // Prove it worked as expected
    if (optionalString.isPresent()) {
      assertThat(sb.toString(), is("HelloHello"));
    } else {
      assertThat(sb.toString(), is("GoodbyeGoodbye"));
    }
  }

  /**
   * Often you need to test a predicate on an object by invoking a method on it (e.g. a getter), but need to do so in a
   * null-safe manner. {@link Optional#filter(java.util.function.Predicate)} supports this case. Note that this filter
   * method is provided directly by the Optional, not the Streams API.
   */
  @Test
  public void testCheckPredicateOnObjectIfNotNull() {
    final String s = StringTestUtils.randomNullString("Hello");
    final StringBuilder sb = new StringBuilder();

    // Old way - Check whether the reference is null, before testing a predicate
    if (s != null && s.length() == 5) {
      sb.append(s);
    }

    // New way - Use Optional.filter() which takes a predicate as an argument. Iff a value is present in the Optional
    // _and_ it matches the predicate, the filter method returns that value; otherwise, it returns an empty Optional
    Optional<String> optionalString = Optional.ofNullable(s); // e.g. what may be returned by the method
    // This example uses a Lambda expression to implement the Predicate passed to filer()
    optionalString.filter(s1 -> s1.length() == 5).ifPresent(s1 -> sb.append(" World!"));

    // Prove it worked as expected
    if (optionalString.isPresent()) {
      assertThat(sb.toString(), is("Hello World!"));
    } else {
      assertThat(sb.toString(), is(""));
    }
  }

  /**
   * The {@link Optional#map(java.util.function.Function)} method can be used to combine the operations of checking
   * whether a value is present and if it is transforming ('mapping') it to another value, otherwise if the value is
   * empty nothing happens.
   */
  @Test
  public void testTransformObjectIfNotNull() {
    // Create a computer without a soundcard
    Computer computer = new Computer(null);

    // Now write the code for getting the USB port of the computer's optional soundcard in a null safe manner -

    // Old way
    Soundcard soundcard = computer.getSoundcard();
    if (soundcard != null) {
      USB usb = soundcard.getUSB();
      // do something with usb...
      assertNull(usb);
    }

    // New way using an Optional - Using Optional.map(), if the Optional has a value then it is "transformed" by the
    // function passed as an argument (in this example a method reference to extract the USB port), while nothing
    // happens if Optional is empty.
    Optional<Soundcard> optionalSoundcard = computer.getSoundcardOptional();
    Optional<USB> optionalUsb = optionalSoundcard.map(Soundcard::getUSB);
    assertThat(optionalUsb.isPresent(), is(false));
  }

  /**
   * Invoking Optional.map() on an existing {@code Optional<T>} results in a nested Optional, i.e.
   * {@code Optional<Optional<T>>}, which is usually not what you want. To get around this Optional also supports a
   * flatMap() method which (in a similar manner to the Stream API's flatMap()) applies a transformation function on the
   * value of the Optional (like map()) but also flattens the resulting two-level Optional into a single one.
   * <p>
   * This example also shows how all the previously described Optional methods can be chained together to write
   * null-safe code for accessing nested data, in a declarative style, rather than nested null checks.
   */
  @Test
  public void testFlattenNestedOptionalObjects() {
    // Create a computer without a soundcard, and hence a USB port
    Computer computer1 = new Computer(null);

    // Now write the code for getting the USB port of the computer's optional soundcard in a null safe manner -

    // Invoking Optional.map() and passing it a function which returns an Optional<T> results in it being wrapped in
    // another Optional, which is probably not what you want...
    @SuppressWarnings("unused")
    Optional<Optional<USB>> optionalOptionalUSB = computer1.getSoundcardOptional().map(Soundcard::getUSBOptional);

    // Use Optional.flatMap() instead to flatten the nested Optional -
    // Also shows how Optional methods can be chained together to write null-safe code without nested null checks -
    String version = computer1.getSoundcardOptional().flatMap(Soundcard::getUSBOptional).map(USB::getVersion).orElse(
        USB.VERSION_UNKNOWN);
    assertThat(version, is(USB.VERSION_UNKNOWN));

    // Prove that the above code also works when the Optional does have a value (are not empty)
    final String usbVersion = "3.0";
    Computer computer2 = new Computer(new Soundcard(new USB(usbVersion)));
    version = computer2.getSoundcardOptional().flatMap(Soundcard::getUSBOptional).map(USB::getVersion).orElse(
        USB.VERSION_UNKNOWN);
    assertThat(version, is(usbVersion));
  }

  static class StringTestUtils {
    private static Random random = new Random();

    public static String randomNullString(String nonNullValue) {
      return random.nextBoolean() ? nonNullValue : null;
    }
  }

  /**
   * The root class in a hierarchy of classes which have optional member fields that are used to support some of the
   * test cases. A Computer has an optional {@link Soundcard}, which in turn has an optional {@link USB} port, which has
   * a version string of interest.
   */
  public class Computer {
    private Soundcard soundcard;

    /**
     * @param soundcard The computer's Soundcard, or null if it doesn't have one.
     */
    public Computer(Soundcard soundcard) {
      this.soundcard = soundcard;
    }

    public Soundcard getSoundcard() {
      return this.soundcard;
    }

    // This method only exists for testing purposes. In practice the member field would be implemented as an
    // Optional<Soundcard>
    public Optional<Soundcard> getSoundcardOptional() {
      return Optional.ofNullable(this.soundcard);
    }
  }

  public class Soundcard {
    private USB usb;

    /**
     * @param usb The Soundcard's USB, or null if it doesn't have one.
     */
    public Soundcard(USB usb) {
      this.usb = usb;
    }

    public USB getUSB() {
      return this.usb;
    }

    // This method only exists for testing purposes. In practice the member field would be implemented as an
    // Optional<USB>
    public Optional<USB> getUSBOptional() {
      return Optional.ofNullable(this.usb);
    }
  }

  public class USB {

    private static final String VERSION_UNKNOWN = "UNKNOWN";

    private String version;

    public USB(String version) {
      this.version = version;
    }

    public String getVersion() {
      return this.version;
    }
  }
}