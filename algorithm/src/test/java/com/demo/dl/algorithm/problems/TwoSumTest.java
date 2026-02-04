package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwoSumTest {

  private TwoSum twoSum;

  @BeforeEach
  void setUp() {
    twoSum = new TwoSum();
  }

  @Test
  void twoSum1() {
    int[] result = twoSum.twoSum(new int[] {2, 7, 11, 15}, 9);
    assertEquals(0, result[0]);
    assertEquals(1, result[1]);
  }

  @Test
  void twoSum2() {
    int[] result = twoSum.twoSum(new int[] {3, 2, 4}, 6);
    assertEquals(1, result[0]);
    assertEquals(2, result[1]);
  }

  @Test
  void twoSum3() {
    int[] result = twoSum.twoSum(new int[] {3, 3}, 6);
    assertEquals(0, result[0]);
    assertEquals(1, result[1]);
  }
}
