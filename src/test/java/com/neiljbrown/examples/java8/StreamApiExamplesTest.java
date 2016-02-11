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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsMapContaining.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Examples of using the new Streams API in Java 8, and its companion <a
 * href="http://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">package of functional
 * interfaces</a> to support processing 'streams'.
 * <p>
 * The <a href="http://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html">Streams API</a> supports
 * implementing a data-processing pipeline - applying a set of ordered operations on an input data-set - using a
 * standard set of functional (filter, map (aka fold) and reduce) methods. The API supports parallel, as well as
 * sequential, processing of data (utilising multi-core CPUs), whilst abstracting the complexity of the underlying
 * multi-threading logic (using Java's existing fork-join framework).
 * <p>
 * A 'Stream' is an abstraction. It’s not a data-structure (like a List or other collection). It’s a pipeline
 * (composition of functions) through which data flows and functions can be applied in composed steps. It’s also
 * non-mutating - it doesn’t modify the source data.
 * <p>
 * The source of a Stream can include Collections, Arrays, and some I/O classes (e.g. BufferedReader).
 */
public class StreamApiExamplesTest {

  /**
   * An example of how {@link java.util.stream.Stream#filter(java.util.function.Predicate)} can be used to declaratively
   * filter a {@link Collection} using a {@link Predicate}.
   * <p>
   * The filter() function eliminates values in the input stream based on a predicate, passing the filtered values ot
   * the output stream. The no. of values in output stream is usually different to input stream. The function replaces
   * the use of 'if' statements in imperative code.
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

    // Before J8 applying the filter required writing imperative code with external (in application code) iteration -
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
   * Example use of {@link Stream#map(java.util.function.Function)} to 'map' (compute) a stream of elements (sourced
   * from a collection) of one type to a stream of elements of another type, declaratively, by applying a
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

    assertThat(studentEmails,
        contains("joe.bloggs@test.net", "jane.bloggs@test.net", "jim.bloggs@test.net", "nellie.bloggs@test.net"));
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

  /**
   * Example use of {@link Stream#distinct()} to remove duplicates from a sequence of elements (e.g. sourced from a
   * Collection).
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

    List<Student> studentsSortedById = students.stream().sorted().collect(Collectors.toList());
    assertThat(studentsSortedById, contains(s1, s2, s3, s4));

    List<Student> studentsSortedByDob = students.stream().sorted(new Student.DobComparator()).collect(
        Collectors.toList());
    assertThat(studentsSortedByDob, contains(s4, s1, s3, s2));
  }

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
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testCreateStreamFromBufferedReader() throws Exception {
    // Create a temp file containing multiple lines
    Path tempFile = Files.createTempFile(this.getClass().getCanonicalName(), ".tmp");
    tempFile.toFile().deleteOnExit();
    BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
    String[] phrase = new String[] { "Everything", "comes", "to", "those", "who", "wait." };
    for (String word : phrase) {
      writer.write(word + System.lineSeparator());
    }
    writer.close();

    BufferedReader reader = Files.newBufferedReader(tempFile, StandardCharsets.UTF_8);
    // BufferedReader.lines() returns a stream containing lines in the Reader, these are only (lazily) read when the
    // terminal operation of the stream is executed - e.g. collect(), which means you can process the contents of a
    // file efficiently, e.g. filter the lines.
    List<String> readPhrase = reader.lines().collect(Collectors.toList());
    reader.close();

    assertThat(readPhrase, is(Arrays.asList(phrase)));
  }

  /**
   * An example of how to use a {@link Collector} which groups (aggregates) elements in a stream by a specified
   * criteria.
   * <p>
   * The {@link Collectors} class provides factory methods for creating implementations of {@link Collector}. These
   * classes provide reduction operations, such as accumulating elements into collections, summarizing elements
   * according to various criteria, etc.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testCollectGroupingBy() throws Exception {
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
      // Create an impl of Collector which reduces the stream by grouping elements by a supplied function  
      Collectors.groupingBy(Student::getDob)
    );
    //@formatter:on

    assertThat(studentsGroupByDob.keySet(), hasSize(3));
    assertThat(studentsGroupByDob, hasEntry(is(s1.getDob()), containsInAnyOrder(s1, s3)));
    assertThat(studentsGroupByDob, hasEntry(is(s2.getDob()), contains(s2)));
    assertThat(studentsGroupByDob, hasEntry(is(s4.getDob()), contains(s4)));
  }

  /**
   * An example of how to process a stream that has no bounds, a so-called 'infinite' stream.
   * <p>
   * This example uses the Streams API to find the total of the square root of the first 'n' prime numbers, starting
   * from 'x'.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testInfiniteStream() throws Exception {
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
      //.peek(i -> System.out.println(i))
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
   * Student domain object. Used to support these examples.
   */
  static class Student implements Comparable<Student> {
    private static int lastId;
    private final int id;
    private final String email;
    private final LocalDate dob;
    private LocalDate graduationDate;
    private final List<ExamResult> examResults;

    public Student(LocalDate dob, String email) {
      this.id = ++Student.lastId;
      this.dob = dob;
      this.email = email;
      this.examResults = new ArrayList<>();
    }

    public final int getId() {
      return this.id;
    }

    public final String getEmail() {
      return this.email;
    }

    public final LocalDate getDob() {
      return this.dob;
    }

    public final LocalDate getGraduationDate() {
      return graduationDate;
    }

    public final void setGraduationDate(LocalDate graduationDate) {
      this.graduationDate = graduationDate;
    }

    public void addExamResult(ExamResult examResult) {
      this.examResults.add(examResult);
    }

    public final List<ExamResult> getExamResults() {
      return examResults;
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
      StringBuilder builder = new StringBuilder();
      builder.append("Student [id=");
      builder.append(id);
      builder.append(", email=");
      builder.append(email);
      builder.append(", dob=");
      builder.append(dob);
      builder.append(", examResults=");
      builder.append(examResults);
      builder.append("]");
      return builder.toString();
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