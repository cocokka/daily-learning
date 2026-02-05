package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThreeSumTest {
  ThreeSum threeSum;

  @BeforeEach
  void setUp() {
    threeSum = new ThreeSum();
  }

  @Test
  void threeSum1() {
    Integer[] nums = new Integer[] {-1, 0, 1, 2, -1, -4};
    List<List<Integer>> results = threeSum.threeSum(nums);
    assertEquals(2, results.size()); // [[-1,-1,2],[-1,0,1]]
  }

  @Test
  void threeSum2() {
    Integer[] nums = new Integer[] {0, 1, 1};
    List<List<Integer>> results = threeSum.threeSum(nums);
    assertEquals(0, results.size()); // [[-1,-1,2],[-1,0,1]]
  }

  @Test
  void threeSum3() {
    Integer[] nums = new Integer[] {0, 0, 0};
    List<List<Integer>> results = threeSum.threeSum(nums);
    assertEquals(1, results.size()); // [[-1,-1,2],[-1,0,1]]
  }
}
