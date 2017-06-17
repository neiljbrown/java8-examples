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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Examples of lambda expressions, implemented as a JUnit test case.
 * <p>
 * A lambda expression is an anonymous function - unlike a method it has no name, and isn't associated with (declared as
 * part of) a class. They can be passed as an argument to a method (a means of passing behaviour), stored in a variable,
 * or returned as a result.
 * <p>
 * A lambda expression has a type of {@link java.lang.FunctionalInterface} when used in assignment statements or passed
 * as a method parameter.
 * <p>
 * The syntax of a lambda expression is like that of a method, comprising: <br>
 * - Typed parameters (the left hand side of the {@code ->}). If the function takes no parameters then you use an empty
 * set of brackets: ().<br>
 * - A body (the right hand side of the {@code ->}) <br>
 * - A return type. For simple cases you can skip the return statement and the compiler will infer the return type.<br>
 * - Declaration of thrown exceptions. <br>
 * For example <br>
 * {@code FileFilter filter = (File f) -> f.getName().endsWith("*.html")}
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

  // ------------------------------------------------------------------------------------------------- Method References

  /**
   * Java 8 provides a new syntax known as a method reference that allows you to use existing methods to pass around
   * behaviour in the same way as a lambda expression, but with a different syntax.
   * <p>
   * Method references can be used when you already have an existing class and method that implements a functional
   * interface, e.g. {@link java.util.function.Predicate}). As well as offering a terser, more readable syntax, a method
   * reference provides better code reuse, and avoids code duplication, when compared to a repeatedly used lambda
   * expression.
   * <p>
   * There are 4 main types of method references: <br>
   * 1) A reference to a static (class-level) method <br>
   * 2) A reference to a method on the object supplied as the first param of the lambda<br>
   * 3) A reference to a method on any other object.<br>
   * 4) A reference to a constructor.
   * <p>
   * This test shows an example of the 2nd type - a reference to a method on the object supplied as the first param of a
   * lambda expression.
   * 
   * @see <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">Oracle Tutorial - Method
   * References</a>
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testMethodReferenceOfTypeInstanceMethodOnFirstParamOfLambda() throws Exception {
    // Example of a lambda expression which takes a single param and invokes a method on it
    FileFilter filter = (File f1) -> f1.canRead();

    File f = Files.createTempFile(this.getClass().getCanonicalName(), ".tmp").toFile();
    f.deleteOnExit();

    // Apply the lambda expression
    File[] files = f.getParentFile().listFiles(filter);
    assertThat(files.length, greaterThanOrEqualTo(1));

    // Lambda expressions like the above can alternatively be implemented using a reference to an existing method -
    // There is a special syntax for this - "{class}::{method-name}"
    // The following example can be read as 'invoke canRead() on the method param which is an instance of File' -
    filter = File::canRead;
    files = f.getParentFile().listFiles(filter);
    assertThat(files.length, greaterThanOrEqualTo(1));
  }

  /**
   * A further example of the use of Method References. As explained in
   * {@link #testMethodReferenceOfTypeInstanceMethodOnFirstParamOfLambda()} there are 4 main types of method references.
   * This test provides an example of a reference to a static (class-level) method.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testMethodReferenceOfTypeStaticMethod() throws Exception {
    // Use static method Integer.parseInt() as a functional interface
    Function<String, Integer> converter = Integer::parseInt;
    assertThat(converter.apply("42"), is(42));
  }

  /**
   * A further example of the use of Method References. As explained in
   * {@link #testMethodReferenceOfTypeInstanceMethodOnFirstParamOfLambda()} there are 4 main types of method references.
   * This test provides an example of a reference to an instance method on any existing object. This type of method
   * reference allows you to (re) use a private method as a parameter to another method, possibly in a different class.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testMethodReferenceOfTypeInstanceMethodOfAnyExistingClass() throws Exception {
    File f = Files.createTempFile("foo", ".secret").toFile();
    f.deleteOnExit();

    // Use a reference to a method in this object to implement a functional interface and pass around behaviour
    FileFilter filter = this::isSecretFile;
    File[] files = f.getParentFile().listFiles(filter);

    assertThat(Arrays.asList(files), hasItem(f));
  }

  private boolean isSecretFile(File f) {
    return f.getName().endsWith(".secret");
  }

  /**
   * Provides an example of how pre Java 8 code to sort a list can be shortened and made easier to read, primarily by
   * reimplementing a Comparator using a lambda expression in place of a anonymous inner class, plus a couple of other
   * new Java 8 language features. Full list of refactorings -
   * <p>
   * 1) Implementation of a Comparator using a lambda expression rather than an anonymous inner class.<br>
   * 2) Using Java 8's new List.sort() instance method in place of Collections.sort() utility method.<br>
   * 3) Using Java 8's new Comparator.comparing() utility method to generate the implementation of a Comparator from a
   * lambda expression which just supplies the method to be used to retrieve the values to be compared.
   * <p>
   * Source of example - Introducing Java 8, Raoul-Gabriel Urma, O'Reilly
   */
  @Test
  public void testRefactorSortingAListUsingLambdaImplementationOfComparator() {
    // Test fixtures
    final List<Invoice> unsortedReadOnlyInvoices = Collections.unmodifiableList(
        Arrays.asList(new Invoice[] {
            new Invoice(1, new BigDecimal("390230.10")),
            new Invoice(2, new BigDecimal("72509.234")),
            new Invoice(3, new BigDecimal("12093810.93")),
            new Invoice(4, new BigDecimal("4783320.98")),
            new Invoice(5, new BigDecimal("7548.32"))
        }));
    final List<Invoice> expectedInvoicesSortedByAmount = Collections.unmodifiableList(
        Arrays.asList(new Invoice[] {
            new Invoice(5, new BigDecimal("7548.32")),
            new Invoice(2, new BigDecimal("72509.234")),
            new Invoice(1, new BigDecimal("390230.10")),
            new Invoice(4, new BigDecimal("4783320.98")),
            new Invoice(3, new BigDecimal("12093810.93"))
        }));

    // Sort a list using a Comparator implemented inline using an anonymous inner class
    List<Invoice> invoices1 = new ArrayList<>(unsortedReadOnlyInvoices);
    Collections.sort(invoices1, new Comparator<Invoice>() {
      @Override
      public int compare(Invoice inv1, Invoice inv2) {
        return inv1.getAmount().compareTo(inv2.getAmount());
      }
    });
    assertThat(invoices1, is(expectedInvoicesSortedByAmount));

    // In Java 8 a Comparator can be implemented as a lambda expression as it is a functional interface - declares a
    // single abstract method. The above can therefore be rewritten as -
    List<Invoice> invoices2 = new ArrayList<>(unsortedReadOnlyInvoices);
    Collections.sort(invoices2, (Invoice inv1, Invoice inv2) -> inv1.getAmount().compareTo(inv2.getAmount()));
    assertThat(invoices2, is(expectedInvoicesSortedByAmount));

    // Java 8 enhances the List interface to support sorting, so the above can be further shortened to -
    List<Invoice> invoices3 = new ArrayList<>(unsortedReadOnlyInvoices);
    invoices3.sort((Invoice inv1, Invoice inv2) -> inv1.getAmount().compareTo(inv2.getAmount()));
    assertThat(invoices3, is(expectedInvoicesSortedByAmount));

    // Java 8 adds a utility method Comparator.comparing() which given a lambda expression to extract a comparable field
    // will generate the Comparator for you, so the above can be further shortened to -
    List<Invoice> invoices4 = new ArrayList<>(unsortedReadOnlyInvoices);
    Comparator<Invoice> comparingInvoiceByAmount = Comparator.comparing((Invoice inv) -> inv.getAmount());
    invoices4.sort(comparingInvoiceByAmount);
    assertThat(invoices4, is(expectedInvoicesSortedByAmount));

    // The Lambda expression can be replaced with a more concise method reference, using a method on the object
    // supplied as the first param of the lambda, so the above can be further shortened to -
    List<Invoice> invoices5 = new ArrayList<>(unsortedReadOnlyInvoices);
    invoices5.sort(Comparator.comparing(Invoice::getAmount));
    assertThat(invoices5, is(expectedInvoicesSortedByAmount));
  }

  class Invoice {
    private final int id;
    private final BigDecimal amount;

    public Invoice(int id, BigDecimal amount) {
      this.id = id;
      this.amount = amount;
    }

    public final int getId() {
      return id;
    }

    public final BigDecimal getAmount() {
      return amount;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Invoice)) {
        System.out.println("Other obj [" + obj.toString() + "] is null or not an invoice");
        return false;
      } else {
        Invoice other = (Invoice) obj;
        if (other.getId() == this.id && other.getAmount().equals(this.amount)) {
          System.out.println("Other invoice [" + other.toString() + "] is equal");
          return true;
        } else {
          System.out.println("Other invoice [" + other.toString() + "] is not equal to this invoice ["
              + this.toString() + "]");
          return false;
        }
      }
    }

    @Override
    public int hashCode() {
      return new Integer(this.id).hashCode() + this.amount.hashCode();
    }

    public String toString() {
      return "id [" + this.id + "], amount [" + this.amount + "]";
    }
  }
}