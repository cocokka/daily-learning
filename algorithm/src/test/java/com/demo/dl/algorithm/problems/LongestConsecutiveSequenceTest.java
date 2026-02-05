package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LongestConsecutiveSequenceTest {

  LongestConsecutiveSequence longestConsecutiveSequence;

  @BeforeEach
  void setUp() {
    longestConsecutiveSequence = new LongestConsecutiveSequence();
  }

  @Test
  void longestConsecutive1() {
    Integer[] nums = new Integer[] {100, 4, 200, 1, 3, 2};
    Integer maximumLength = longestConsecutiveSequence.longestConsecutive(nums);
    assertEquals(4, maximumLength);
  }

  @Test
  void longestConsecutive2() {
    Integer[] nums = new Integer[] {0, 3, 7, 2, 5, 8, 4, 6, 0, 1};
    Integer maximumLength = longestConsecutiveSequence.longestConsecutive(nums);
    assertEquals(9, maximumLength);
  }

  @Test
  void longestConsecutive3() {
    Integer[] nums = new Integer[] {1, 0, 1, 2};
    Integer maximumLength = longestConsecutiveSequence.longestConsecutive(nums);
    assertEquals(3, maximumLength);
  }

  @Test
  void longestConsecutive4() {
    Integer[] nums = new Integer[] {1};
    Integer maximumLength = longestConsecutiveSequence.longestConsecutive(nums);
    assertEquals(1, maximumLength);
  }
}
