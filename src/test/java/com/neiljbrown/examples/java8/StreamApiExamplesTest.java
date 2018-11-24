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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.collection.IsMapContaining.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.*;

import org.junit.Test;

/**
 * Examples of using the new <a
 * href="http://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html">Streams API</a> in Java 8, and
 * its companion <a href="http://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">package of
 * functional interfaces</a> for Stream processing, implemented as a set of JUnit Test Cases.
 * <p>
 * The Streams API supports processing sequences of data from supporting data-sources, using a set of high-level,
 * declarative-style operations, allowing you to focus on the what rather than the how of data processing. The API
 * supports parallel, as well as sequential, processing of data (utilising multi-core CPUs), whilst abstracting the
 * complexity of the underlying multi-threading logic (using Java's existing fork-join framework).
 * <p>
 * A <a href="http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html">Stream</a> can be defined as a
 * sequence of elements of the same type from a data-source which supports aggregate operations. The supported
 * operations are a mix of database-like operations (e.g. findFirst, allMatch, sorted) and functional programming
 * methods (e.g. filter, map, reduce). A Stream is not a data-structure (like a List or other type of Collection). It
 * doesn't store the elements, but rather consumes and processes them from a source, on demand. Data-sources which
 * support generating a Stream include Collections, Arrays, and some I/O classes (e.g. BufferedReader).
 * <p>
 * Stream operations have the following characteristics -
 * <ul>
 * <li>Non-mutating - Stream operations never modify the source data.</li>
 * <li>Support Pipelining - Many stream operations return a stream themselves. This allows the operations to be chained
 * together to form larger, data-processing 'pipelines'. This in turn enables optimisations such as lazy evaluation,
 * short-circuiting and loop fusion.</li>
 * <li>Internalise Iteration - Processing a data-source using the Collections API requires (imperative) code to be
 * written to iterate (loop) over each value. In contrast, Stream operations encapsulate details of how the data
 * elements are processed, internalising any iteration of the values.</li>
 * </ul>
 * <p>
 * Stream operations can be grouped into two categories:
 * <ol>
 * <li>Intermediate operations - These are Stream operations which are used to commence and continue processing the
 * Stream. They return a Stream, and can therefore be connected together to form a pipeline. They are lazily evaluated
 * and can often be optimised. Examples of these operations include filter, map, sorted, distinct.</li>
 * <li>Terminal operations - These are Stream operations which are used to terminate a Stream's data-processing pipeline
 * and return a result of another type, such as a List or Integer. Examples of these operations include collect,
 * findFirst, and allMatch.</li>
 * </ol>
 * 
 * @see IntStreamExamplesTest
 */
public class StreamApiExamplesTest {

  // ------------------------------------------------------------------------------------------------- Filter operations
  // There are several types of operations that can be used to filter elements in a Stream.

  /**
   * An example of how {@link java.util.stream.Stream#filter(java.util.function.Predicate)} can be used to declaratively
   * filter a {@link Collection} using a {@link Predicate}.
   * <p>
   * The filter() function eliminates values in the input stream based on a predicate, passing the filtered values to
   * the output stream. The no. of values in output stream is usually different to the input stream. The function
   * replaces the use of 'for' and 'if' statements in imperative code.
   */
  @Test
  public void testFilter() {
    // Create a list of students with different DOB
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s4);

    // Filter the list to only contain those born after 1975
    int yearOfBirthFilter = 1975;

    // Pre J8 applying a filter required writing imperative code with external (in application code) iteration -
    List<Student> filteredStudents = new ArrayList<>(students.size());
    for (Student student : students) {
      if (student.getDob().getYear() > yearOfBirthFilter) {
        filteredStudents.add(student);
      }
    }
    assertThat(filteredStudents, contains(s2, s3));

    // From J8 onwards, the new Streams API can be used to apply the filter
    filteredStudents = students
        // Use Collection.stream() to create a java.util.stream.Stream (sequence of elements supporting sequential and
        // parallel aggregate operations) for operations on this Collection
        .stream()
        // Use Stream.filter() to filter the List of Students according to a java.util.function.Predicate, the
        // test() method of which is implemented as a Lambda expression
        .filter(s -> s.getDob().getYear() > yearOfBirthFilter)
        // Accumulate the elements of the stream back into a List
        // The intermediate operations are lazily evaluated. It's only when this terminal operation on the Stream is
        // performed that the processing takes place.
        .collect(Collectors.toList());

    assertThat(filteredStudents, contains(s2, s3));
  }

  /**
   * Example use of {@link Stream#distinct()} to remove duplicates from a sequence of elements (e.g. sourced from a
   * Collection), returning only the unique ones.
   */
  @Test
  public void testDistinct() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    students.add(s3);
    // Add a duplicate Student to the list
    students.add(s2);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s4);

    List<Student> uniqueStudents = students.stream().distinct().collect(Collectors.toList());

    assertThat(uniqueStudents, contains(s1, s2, s3, s4));
    assertThat(uniqueStudents, hasSize(students.size() - 1));
  }

  /**
   * Example use of {@link Stream#limit} to return a stream which contains at most 'n' elements. This method is
   * typically used to return the first 'n' elements of a resulting, secondary stream after earlier operation(s) have
   * processed all the elements.
   * <p>
   * As noted in the method's API docs, limit() is generally a cheap operation on sequential stream pipelines, but it
   * can be quite expensive on _ordered_ parallel pipelines.
   */
  @Test
  public void testLimit() {
    // Create a list of students with different DOB
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s4);

    // This example tackles the problem of how to get the two oldest students, in ascending order (oldest first)
    final List<Student> expectedStudents = Arrays.asList(s4, s1);

    // Pre J8 implementation using imperative logic
    List<Student> oldestTwoStudents = new ArrayList<>(2);
    Student oldestStudent = null;
    Student secondOldestStudent = null;
    for (Student s : students) {
      if (oldestStudent == null) {
        oldestStudent = s;
      } else if (s.getDob().isBefore(oldestStudent.getDob())) {
        secondOldestStudent = oldestStudent;
        oldestStudent = s;
      } else if (secondOldestStudent == null || s.getDob().isBefore(secondOldestStudent.getDob())) {
        secondOldestStudent = s;
      }
    }
    oldestTwoStudents.add(oldestStudent);
    oldestTwoStudents.add(secondOldestStudent);
    assertThat(oldestTwoStudents, is(expectedStudents));

    // Pre J8 implementation - Improved solution. Removes need for loop by sorting list first, then generating sublist
    List<Student> studentsSortedByDob = new ArrayList<>(students);
    studentsSortedByDob.sort(new Comparator<Student>() {
      @Override
      public int compare(Student s1, Student s2) {
        return s1.getDob().compareTo(s2.getDob());
      }
    });
    oldestTwoStudents = studentsSortedByDob.subList(0, 2);
    assertThat(oldestTwoStudents, is(expectedStudents));

    // From J8 onwards - Sort list (using Lambda expr to implement comparator), then apply a limit to resulting stream
    oldestTwoStudents = students.stream()
        .sorted((stud1, stud2) -> stud1.getDob().compareTo(stud2.getDob()))
        .limit(2)
        .collect(Collectors.toList());
    assertThat(oldestTwoStudents, is(expectedStudents));
  }

  // ---------------------------------------------------------------------------------------------------- Map operations

  /**
   * Example use of {@link Stream#map(java.util.function.Function)} to 'map' (compute) a stream of elements (sourced
   * from a collection) of one type to a stream of elements of another type, in a declarative way, by applying a
   * {@link java.util.function.Function} to each individual element.
   * <p>
   * The map() function transforms the type in the input stream to another type in the output stream. The no. of values
   * in the output stream is always same as input stream. Allows you to apply a computation to the input stream.
   */
  @Test
  public void testMap() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s4);

    // Create a list of all the student's email addresses
    // Uses a lambda expression to implement the Function passed to map(), using the method reference "::" syntax
    List<String> studentEmails = students.stream().map(Student::getEmail).collect(Collectors.toList());

    assertThat(studentEmails, contains(s1.getEmail(), s2.getEmail(), s3.getEmail(), s4.getEmail()));
  }

  /**
   * Example use of {@link Stream#flatMap(java.util.function.Function)} which has the effect of applying a one-to-many
   * transformation on the elements of the stream (e.g. by retrieving a non-scalar property), and then flattening the
   * resulting elements into a new stream.
   */
  @Test
  public void testFlatMap() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    s1.addExamResult(new ExamResult("Maths", 75));
    s1.addExamResult(new ExamResult("Physics", 69));
    s1.addExamResult(new ExamResult("Chemistry", 84));
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    s2.addExamResult(new ExamResult("English Literature", 87));
    s2.addExamResult(new ExamResult("History", 72));
    students.add(s2);

    // Create a list of all the student's exam results -
    // Transforms the stream of students to a stream of their exam results
    // Uses a lambda expression to implement the Function passed to flatMap()
    List<ExamResult> examResults = students.stream().flatMap(student -> student.getExamResults().stream()).collect(
        Collectors.toList());

    List<ExamResult> expectedExamResults = new ArrayList<>(s1.getExamResults());
    expectedExamResults.addAll(s2.getExamResults());
    assertThat(examResults, is(expectedExamResults));
  }

  // ------------------------------------------------------------------------------------------------- Reduce operations
  // Reduction operations concern combining elements from a source to yield a single value, e.g. sum values, max value.
  // Reduce operations repeatedly apply an operation to each element until a result is produced.

  /**
   * Example of how to use the general-purpose {@link Stream#reduce(Object, java.util.function.BinaryOperator)}
   * operation to sum a list of numbers. This is a terminal Stream operation.
   */
  @Test
  public void testReduce() {
    // Test fixture - A list of Students, each with student fees that need to be totaled.
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "jo.bloggs@test.net", new BigDecimal("891.32"));
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "ja.bloggs@test.net", new BigDecimal("16.99"));
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "ji.bloggs@test.net", new BigDecimal("578.50"));
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nel.bloggs@test.net", new BigDecimal("95.00"));
    students.add(s4);

    // This example solves the requirement to calculate the total fees paid by all the students
    BigDecimal expectedTotalFees = s1.getFee().add(s2.getFee()).add(s3.getFee()).add(s4.getFee());

    // Pre Java 8, calculating the sum of a list of numbers entailed using a loop -
    BigDecimal totalFees = new BigDecimal("0.00");
    for (Student s : students) {
      totalFees = totalFees.add(s.getFee());
    }
    assertThat(totalFees, is(expectedTotalFees));

    // From Java 8 onwards, Stream.reduce() can be used to sum the numbers, by applying a BinaryOperator to each element
    // which combines two elements to produce a new value.
    totalFees = students.stream()
        .map(s -> s.getFee())
        // The first param is the initial value of the result.
        // The second param is the operation for combining the elements of the list, in this case an addition operation
        .reduce(new BigDecimal("0.00"), (fee1, fee2) -> fee1.add(fee2));
    assertThat(totalFees, is(expectedTotalFees));
  }

  /**
   * Example of how to use the {@link Stream#max(Comparator)} Stream operation to return the 'maximum' element of a
   * stream, according to a supplied Comparator.
   * <p>
   * The max() operation is a special-case of a reduction operation which is so common that the Streams API provides an
   * implementation out-of-the-box.
   */
  @Test
  public void testMax() {
    // Test fixture - A list of Students, each with student fees for which we want to find the maximum
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "jo.bloggs@test.net", new BigDecimal("291.32"));
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "ja.bloggs@test.net", new BigDecimal("16.99"));
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "ji.bloggs@test.net", new BigDecimal("578.50"));
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nel.bloggs@test.net", new BigDecimal("95.00"));
    students.add(s4);

    // This example solves the requirement to find the max fee paid by a student
    BigDecimal expectedMaxFee = s3.getFee();

    // Pre Java 8, finding the max of a list of numbers entailed using a loop, and temp variable -
    BigDecimal maxFee = new BigDecimal("0.00");
    for (Student s : students) {
      if (s.getFee().compareTo(maxFee) > 0) {
        maxFee = s.getFee();
      }
    }
    assertThat(maxFee, is(expectedMaxFee));

    // From Java 8 onwards, Stream.max() can be used to find the max element of a list
    Optional<BigDecimal> optionalMaxFee = students.stream()
        .map(s -> s.getFee())
        .max((fee1, fee2) -> fee1.compareTo(fee2));
    assertThat(optionalMaxFee.orElse(new BigDecimal("0.00")), is(expectedMaxFee));
  }

  // -------------------------------------------------------------------------------------------------- Match operations
  // Use Match stream operations to determine whether any, some or no elements in the stream match a given property.
  // All Match operations take a Predicate as an argument and return a boolean result.

  /**
   * Example use of {@link Stream#anyMatch(Predicate)} to determine whether any (one or more) elements in the stream
   * match a specified predicate (condition).
   */
  @Test
  public void testAnyMatch() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    s1.addExamResult(new ExamResult("Maths", 96));
    s1.addExamResult(new ExamResult("Physics", 69));
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    s2.addExamResult(new ExamResult("English Literature", 87));
    s2.addExamResult(new ExamResult("History", 72));
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    s2.addExamResult(new ExamResult("Chemistry", 54));
    s2.addExamResult(new ExamResult("Maths", 90));
    students.add(s3);

    boolean expertExists = students.stream()
        .flatMap(student -> student.getExamResults().stream())
        // Are there any experts?
        .anyMatch(er -> er.getScore() > 95);
    assertThat(expertExists, is(true));

    boolean geniusExists = students.stream()
        .flatMap(student -> student.getExamResults().stream())
        // Are there any geniuses?
        .anyMatch(er -> er.getScore() == 100);
    assertThat(geniusExists, is(false));

    boolean geographyStudentsExist = students.stream()
        .flatMap(student -> student.getExamResults().stream())
        // Did anybody study sit an exam for this subject?
        .anyMatch(er -> er.getExam().equalsIgnoreCase("geography"));
    assertThat(geographyStudentsExist, is(false));
  }

  // --------------------------------------------------------------------------------------------------- Find operations
  // The Stream interface provides find operations for retrieving the first or any arbitrary element from a stream.

  /**
   * Example use of {@link Stream#findFirst()} to return the first element in the stream.
   */
  @Test
  public void testFindFirst() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);

    Optional<Student> s = students.stream().findFirst();
    assertThat(s.get(), is(s1));

    // Stream.findFirst() returns an Optional to handle the case where the stream is empty (contains no elements)
    final List<Student> noStudents = Collections.emptyList();
    s = noStudents.stream().findFirst();
    assertThat(s.isPresent(), is(false));
  }

  // --------------------------------------------------------------------------------------------------- Sort operations

  /**
   * Example use of {@link Stream#sorted()} and {@link Stream#sorted(java.util.Comparator)} to sort a sequence of
   * elements (e.g. sourced from a Collection) by their natural order, or a supplied Comparator.
   */
  @Test
  public void testSorted() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    final Student s3 = new Student(LocalDate.of(1976, Month.AUGUST, 7), "jim.bloggs@test.net");
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s2);
    students.add(s1);
    students.add(s3);
    students.add(s4);

    //@formatter:off
    List<Student> studentsSortedById = students.stream()
        .sorted()
        .collect(Collectors.toList());
    //@formatter:on
    assertThat(studentsSortedById, contains(s1, s2, s3, s4));

    List<Student> studentsSortedByDob = students.stream().sorted(new Student.DobComparator()).collect(
        Collectors.toList());
    assertThat(studentsSortedByDob, contains(s4, s1, s3, s2));
  }

  // ------------------------------------------------------------------------------------------------ Collect operations
  // Collect operations are terminal Stream operations, like reduction, that allow you to accumulate (aggregate)
  // elements into a summary result. In some cases they can also be used to convert from one type to another at
  // aggregation time.
  //
  // Collect operations on a Stream use the java.util.stream.Collector interface to describe how to accumulate the
  // elements. The java.util.stream.Collectors (plural) interface provides factory methods for creating various 
  // implementations of Collector.
  //
  // Collector exist to convert a stream to various types of java.util.Collection. As a result the Stream.collect()
  // methods are commonly used, in conjunction with other functional methods, to replace pre-Java 8 imperative code / 
  // external loops for converting one type of collection to another.

  /**
   * An example of how to use a {@link Collector} which groups (aggregates) elements in a stream by a specified
   * criteria. Uses {@link Collectors#groupingBy(Function)} to create a group-by operation which uses a single
   * function.
   * <p>
   * The {@link Collectors} class provides factory methods for creating implementations of {@link Collector}. These
   * classes provide reduction operations, such as accumulating elements into collections, summarizing elements
   * according to various criteria, etc.
   */
  @Test
  public void testCollectGroupingBy() {
    // Create a list of students in which more than one has the same date of birth
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    students.add(s2);
    final Student s3 = new Student(s1.getDob(), "jim.bloggs@test.net");
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    students.add(s4);

    //@formatter:off
    Map<LocalDate, List<Student>> studentsGroupByDob = students.stream().collect(
      // Create impl of Collector which reduces the stream by grouping elements by a supplied function  
      groupingBy(Student::getDob)
    );
    //@formatter:on

    // The Collectors.groupingBy() Collector returns a Map representing the aggregated stream, keyed by grouping type
    assertThat(studentsGroupByDob.keySet(), hasSize(3));
    assertThat(studentsGroupByDob, hasEntry(is(s1.getDob()), containsInAnyOrder(s1, s3)));
    assertThat(studentsGroupByDob, hasEntry(is(s2.getDob()), contains(s2)));
    assertThat(studentsGroupByDob, hasEntry(is(s4.getDob()), contains(s4)));
  }

  /**
   * An example of creating and using a so-called 'cascading' group-by operation that first groups elements in the
   * stream by applying a supplied classification function (as shown in example {@link #testCollectGroupingBy()},
   * then additionally applies a reduction operation on the values using a second supplied Collector. A cascading
   * group-by operation is created using {@link Collectors#groupingBy(Function, Collector)}.
   */
  @Test
  public void testCollectCascadingGroupByWithSecondaryFunction() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    s1.setCountry("UK");
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    s2.setCountry("Netherlands");
    students.add(s2);
    final Student s3 = new Student(s1.getDob(), "jim.bloggs@test.net");
    s3.setCountry("Sweden");
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1973, Month.JULY, 12), "nellie.bloggs@test.net");
    s4.setCountry("UK");
    students.add(s4);

    // Compute the set of email addresses of students in each country
    Map<String, Set<String>> emailsByCountry =
      students.stream().collect(groupingBy(Student::getCountry,mapping(Student::getEmail, toSet())));

    assertThat(emailsByCountry.keySet(), hasSize(3));
    assertThat(emailsByCountry, hasEntry(is(s1.getCountry()), containsInAnyOrder(s1.getEmail(), s4.getEmail())));
    assertThat(emailsByCountry, hasEntry(is(s2.getCountry()), contains(s2.getEmail())));
    assertThat(emailsByCountry, hasEntry(is(s3.getCountry()), contains(s3.getEmail())));
  }

  /**
   * An example of how to use {@link Collectors#joining} to obtain an implementation of a {@link Collector} that
   * concatenates a stream of strings in the order they're processed
   * <p>
   * This collection operation is useful when converting (mapping) an input stream of objects to a stream of strings
   * that subsequently need reporting as one. This example uses the overloaded method
   * {@link Collectors#joining(CharSequence)} to add an optional delimiter between the concatenated strings. There are
   * also overloaded methods for adding optional prefixes and suffixes to each string.
   */
  @Test
  public void testCollectJoining() {
    List<FieldError> fieldErrors = new ArrayList<>();
    FieldError error1 = new FieldError("firstName", "invalid first name");
    fieldErrors.add(error1);
    FieldError error2 = new FieldError("lastName", "invalid last name");
    fieldErrors.add(error2);

    String errorMessages = fieldErrors.stream()
        .map((fieldError) -> "Field [" + fieldError.getFieldName() + "] " + fieldError.getMessage())
        // Create impl of Collector which reduces the stream by concatenating its elements, separated by delimiter
        .collect(Collectors.joining(System.lineSeparator()));

    assertThat(errorMessages, not(isEmptyOrNullString()));
    assertThat(errorMessages, startsWith("Field [" + error1.getFieldName()));
    assertThat(errorMessages, containsString("Field [" + error2.getFieldName()));
  }

  /**
   * An example of how to use
   * {@link Collectors#toConcurrentMap(java.util.function.Function, java.util.function.Function)} to convert an input
   * Map to another type of Map.
   */
  @Test
  public void testCollectConvertAMap() {
    Map<String, FieldError> fieldErrors = new HashMap<>();
    FieldError error1 = new FieldError("firstName", "invalid first name");
    fieldErrors.put(error1.getFieldName(), error1);
    fieldErrors.put("invalid", null);
    FieldError error2 = new FieldError("lastName", "invalid last name");
    fieldErrors.put(error2.getFieldName(), error2);

    // Convert a Map<String,FieldError> to a Map<String,String>
    ConcurrentMap<String, String> fieldErrorMessages = fieldErrors.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        // Use Collectors.toConcurrentMap() to convert the stream's map entry key and value to a new type and
        // aggregate the new map entries into a new Map
        .collect(Collectors.toConcurrentMap(entry -> entry.getKey(),
            entry -> entry.getValue().getMessage()));

    assertThat(fieldErrorMessages.keySet(), hasSize(2));
    assertThat(fieldErrorMessages, hasEntry(is(error1.getFieldName()), is(error1.getMessage())));
    assertThat(fieldErrorMessages, hasEntry(is(error2.getFieldName()), is(error2.getMessage())));
  }

  class FieldError {
    private String fieldName;
    private String message;

    public FieldError(String fieldName, String message) {
      this.fieldName = fieldName;
      this.message = message;
    }

    public final String getFieldName() {
      return this.fieldName;
    }

    public final String getMessage() {
      return this.message;
    }
  }

  // ------------------------------------------------------------------------- Create Streams from data-sources & values

  /**
   * Collections are not the only thing that can be used as the source of streams. The Streams API supports creating a
   * Stream from a supplied value or list of values as well, using e.g. {@link Stream#of(Object...)}.
   */
  @Test
  public void testCreateStreamFromSuppliedValues() {
    String[] phrase = new String[] { "Everything", "comes", "to", "those", "who", "wait." };

    Stream<String> stream = Stream.of(phrase[0], phrase[1], phrase[2], phrase[3], phrase[4], phrase[5]);

    assertThat(stream.collect(Collectors.toList()), is(Arrays.asList(phrase)));
  }

  /**
   * Collections are not the only thing that can be used as the source of streams. {@link Arrays} provides various
   * methods for creating a stream from different types of arrays, including e.g. {@link Arrays#stream(Object[])}.
   */
  @Test
  public void testCreateStreamFromArray() {
    String[] phrase = new String[] { "Everything", "comes", "to", "those", "who", "wait." };

    Stream<String> stream = Arrays.stream(phrase);

    assertThat(stream.collect(Collectors.toList()), is(Arrays.asList(phrase)));
  }

  /**
   * Collections are not the only thing that can be used as the source of streams. {@link BufferedReader#lines} returns
   * a stream of all the lines in a Reader.
   * 
   * @throws Exception if an unexpected error occurs.
   */
  @Test
  public void testCreateStreamFromBufferedReader() throws Exception {
    // Create a temp file containing multiple lines
    Path tempFile = Files.createTempFile(this.getClass().getCanonicalName(), ".tmp");
    tempFile.toFile().deleteOnExit();
    String[] phrase = new String[] { "Everything", "comes", "to", "those", "who", "wait." };
    Files.write(tempFile, Arrays.asList(phrase), StandardCharsets.UTF_8);

    BufferedReader reader = Files.newBufferedReader(tempFile, StandardCharsets.UTF_8);
    // BufferedReader.lines() returns a stream containing lines in the Reader, these are only (lazily) read when the
    // terminal operation of the stream is executed - e.g. collect(), which means you can process the contents of a
    // file efficiently, e.g. filter the lines.
    List<String> readPhrase = reader.lines().collect(Collectors.toList());
    reader.close();

    assertThat(readPhrase, is(Arrays.asList(phrase)));
  }

  /**
   * The {@link Iterable} interface was introduced in Java 5, to support classes declaring themselves as targets of the
   * then new for-each loop statement. In J8, it is also possible to process an instance of an {@link Iterable} in a
   * functional manner using the Streams API, but only by creating a {@link Spliterator} over the {@link Iterable},
   * and using a utility method provided by {@link StreamSupport}.
   *
   * <h2>Resources</h2>
   * https://stackoverflow.com/questions/23932061/convert-iterable-to-stream-using-java-8-jdk/23936723#23936723
   * <p>
   * https://www.journaldev.com/13521/java-spliterator
   *
   * @see StreamSupport#stream(Spliterator, boolean)
   */
  @Test
  public void testCreateStreamFromIterableViaSpliterator() {
    final List<String> strings = Arrays.asList("a", "b", "c");

    // Pre J8 code illustrating use of Iterable -- as target of for-each
    for(String s : strings) {
      assertThat(strings, hasItem(s));
    }

    // Simulate an API that is passed an instance of Iterable (java.util.List implements java.lang.Iterable)
    final Iterable<String> stringsIterable = strings;

    // In J8, it's possible to use functional methods provided by the Stream APIs to process elements of an Iterable,
    // but you need to convert it to a Spliterator first, and use the StreamSupport utility class to create the stream -
    final long count = StreamSupport.stream(stringsIterable.spliterator(), false).count();
    assertThat(count, is(Long.valueOf(strings.size())));
  }

  // ------------------------------------------------------------------------------------------- Other Stream operations

  /**
   * An example of how operations on {@link Stream} can be chained together to implement a data-processing pipeline.
   * <p>
   * In this example the highest score that any student was awarded for a particular subject in a particular graduation
   * year is determined declaratively by chaining operations on a Collection's stream.
   * <p>
   * Also illustrates the use of one of the classes of {@link Stream} used for primitive elements -
   * {@link java.util.stream.IntStream}, and the terminal reduction stream function
   * {@link java.util.stream.IntStream#max}.
   */
  @Test
  public void testChainStreamOperations() {
    final List<Student> students = new ArrayList<>();
    final Student s1 = new Student(LocalDate.of(1974, Month.JUNE, 21), "joe.bloggs@test.net");
    s1.setGraduationDate(LocalDate.parse("2014-09-12"));
    s1.addExamResult(new ExamResult("Maths", 75));
    s1.addExamResult(new ExamResult("Physics", 69));
    s1.addExamResult(new ExamResult("Chemistry", 84));
    students.add(s1);
    final Student s2 = new Student(LocalDate.of(1980, Month.JANUARY, 2), "jane.bloggs@test.net");
    s2.setGraduationDate(LocalDate.parse("2014-09-12"));
    s2.addExamResult(new ExamResult("English Literature", 87));
    s2.addExamResult(new ExamResult("History", 72)); // Didn't sit Math
    students.add(s2);
    final Student s3 = new Student(LocalDate.of(1974, Month.JUNE, 21), "jack.bloggs@test.net");
    s3.setGraduationDate(LocalDate.parse("2014-09-12"));
    s3.addExamResult(new ExamResult("Maths", 76)); // <-- The top Math score in 2014
    s3.addExamResult(new ExamResult("Geography", 75));
    students.add(s3);
    final Student s4 = new Student(LocalDate.of(1974, Month.JULY, 12), "jenny.bloggs@test.net");
    s4.setGraduationDate(LocalDate.parse("2013-09-12")); // Sat Math, but graduated a year earlier
    s4.addExamResult(new ExamResult("Chemistry", 65));
    s4.addExamResult(new ExamResult("Maths", 82));
    students.add(s4);

    //@formatter:off
    OptionalInt optionalInt = students.stream()
      .filter(s -> s.getGraduationDate().getYear() == 2014)
      .flatMap(s -> s.getExamResults().stream())
      .filter(er -> "Maths".equals(er.getExam()))
      .mapToInt(er -> er.getScore()) // Use specialised version of map function to get an IntStream
      .max(); // IntStream.max() is a short-cut for Stream.reduce(Integer::max)
    //@formatter:on    

    assertThat(optionalInt.isPresent(), is(true));
    assertThat(optionalInt.getAsInt(), is(76));
  }

  /**
   * An example of how to process a stream that has no bounds, a so-called 'infinite' stream.
   * <p>
   * This example uses the Streams API to find the total of the square root of the first 'n' prime numbers, starting
   * from 'x'.
   */
  @Test
  public void testInfiniteStream() {
    final int numberOfPrimesToFind = 5;
    final int startValue = 2;
    final double expectedResult = Math.sqrt(2.0) + Math.sqrt(3.0) + Math.sqrt(5.0) + Math.sqrt(7.0) + Math.sqrt(11.0);

    // The imperative implementation of this function is verbose, requiring a loop and several temporary variables.
    // Like all imperative code, it’s all about the how, and not the why - so it’s less readable -
    double total = 0.0;
    int countedPrimes = 0;
    int nextValue = startValue;
    while (countedPrimes < numberOfPrimesToFind) {
      if (isPrime(nextValue)) {
        total += Math.sqrt(nextValue);
        countedPrimes++;
      }
      nextValue++;
    }
    assertThat(total, is(expectedResult));

    // The functional way -
    // Create an infinite stream (one with no bounds) of integers. This example starts at 1 and increments by 1.
    total = Stream.iterate(startValue, e -> e + 1)
        // Filter the stream to create an infinite stream of prime numbers
        .filter(i -> isPrime(i))
        // Uncomment this line to debug the found primes
        // .peek(i -> System.out.println(i))
        // Only process the first ‘n’ prime numbers from the stream
        .limit(numberOfPrimesToFind)
        // Convert the stream of prime numbers to their square roots
        .map(i -> Math.sqrt(i))
        // Sum the values, using the reduce function
        .reduce(0.0, Double::sum);
    assertThat(total, is(expectedResult));
  }

  /**
   * @param number The number to be tested.
   * @return true if a supplied number is a prime number - a no. greater than 1, that is divisible only by 1 and itself.
   */
  private static final boolean isPrime(int number) {
    return number > 1 &&
        // Functional way to check that a number is only divisible by 1 and itself, and not values in between
        IntStream.range(2, number).noneMatch(i -> number % i == 0);
  }

  /**
   * An example of how {@link Stream#concat(Stream, Stream)} can be used to (lazily) combine (concatenate) two or
   * more Collections, e.g. Lists.
   * <p>
   * In J7, you could simply use {@link Collection#addAll(Collection)}, however it is worth noting that this is
   * slightly less efficient, as the method creates a new collection with additional references to the same objects
   * that are in the first two collections.
   * <p>
   * This example shows combing two Lists. If you need to combine more than two Collections you can nest the
   * invocations of Stream.concat(), i.e. use the result of Stream.concat(Stream, Stream) as one of the params.
   * <p>
   * Alternative solutions for pre-Java 8 include
   * <p>
   * - Guava's <a href="https://google.github.io/guava/releases/21.0/api/docs/com/google/common/collect/Iterables.html#concat-java.lang.Iterable-java.lang.Iterable-">Iterabales.concat(Iterable, Iterable)</a>
   * method, or one of its overloaded variants.
   * <p>
   * For more details see https://www.baeldung.com/java-combine-multiple-collections
   */
  @Test
  public void testConcatenateTwoLists() {
    // (Note - From Java 9+ use Collection static factory method List.of(...) to create immutable lists instead).
    final List<String> strings1 = Arrays.asList("a", "b", "c");
    final List<String> strings2 = Arrays.asList("d", "e", "f");

    // Pre J8 (J7) solution -
    List<String> combinedStrings = new ArrayList<>();
    combinedStrings.addAll(strings1);
    combinedStrings.addAll(strings2);

    // J8 solution - More efficient -
    List<String> moreCombinedStrings =
      Stream.concat(strings1.stream(), strings2.stream()).collect(Collectors.toList());

    assertThat(moreCombinedStrings, contains("a", "b", "c", "d", "e", "f"));
  }

  // Test Fixtures, Supporting Classes

  /**
   * Student domain object. Used to support these examples.
   */
  static class Student implements Comparable<Student> {
    private static int lastId;

    private static final BigDecimal STANDARD_FEE = new BigDecimal(650.72);

    private final int id;
    private final String email;
    private final LocalDate dob;
    private LocalDate graduationDate;
    private final List<ExamResult> examResults;
    private final BigDecimal fee;
    private String country;

    Student(LocalDate dob, String email) {
      this(dob, email, STANDARD_FEE);
    }

    Student(LocalDate dob, String email, BigDecimal fee) {
      this.id = ++Student.lastId;
      this.dob = dob;
      this.email = email;
      this.examResults = new ArrayList<>();
      this.fee = fee;
    }

    final int getId() {
      return this.id;
    }

    final String getEmail() {
      return this.email;
    }

    final LocalDate getDob() {
      return this.dob;
    }

    final LocalDate getGraduationDate() {
      return graduationDate;
    }

    final void setGraduationDate(LocalDate graduationDate) {
      this.graduationDate = graduationDate;
    }

    void addExamResult(ExamResult examResult) {
      this.examResults.add(examResult);
    }

    final List<ExamResult> getExamResults() {
      return examResults;
    }

    final BigDecimal getFee() {
      return this.fee;
    }

    String getCountry() {
      return this.country;
    }

    void setCountry(String country) {
      this.country = country;
    }

    @Override
    public int compareTo(Student s) {
      return (this.id < s.getId()) ? -1 : (this.id == s.getId()) ? 0 : 1;
    }

    // auto-generated
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + id;
      return result;
    }

    // auto-generated
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Student)) {
        return false;
      }
      Student other = (Student) obj;
      if (id != other.id) {
        return false;
      }
      return true;
    }

    // auto-generated


    @Override
    public String toString() {
      return "Student{" +
        "id=" + id +
        ", email='" + email + '\'' +
        ", dob=" + dob +
        ", graduationDate=" + graduationDate +
        ", examResults=" + examResults +
        ", fee=" + fee +
        ", country='" + country + '\'' +
        '}';
    }

    static class DobComparator implements Comparator<Student> {
      @Override
      public int compare(Student s1, Student s2) {
        return s1.getDob().isBefore(s2.getDob()) ? -1 : s1.getDob().equals(s2.getDob()) ? 0 : 1;
      }
    }

  }

  static class ExamResult {
    private final String exam;
    private final int score;

    public ExamResult(String exam, int score) {
      this.exam = exam;
      this.score = score;
    }

    public final String getExam() {
      return this.exam;
    }

    public final int getScore() {
      return this.score;
    }

    // auto-generated
    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ExamResult [exam=");
      builder.append(exam);
      builder.append(", score=");
      builder.append(score);
      builder.append("]");
      return builder.toString();
    }
  }
}