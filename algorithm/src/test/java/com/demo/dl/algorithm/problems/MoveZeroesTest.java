package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveZeroesTest {
  MoveZeroes moveZeroes;

  @BeforeEach
  void setUp() {
    moveZeroes = new MoveZeroes();
  }

  @Test
  void moveZeroes1() {
    int[] nums = new int[] {0, 1, 0, 3, 12};
    moveZeroes.moveZeroes(nums);
    assertEquals(1, nums[0]);
    assertEquals(3, nums[1]);
    assertEquals(12, nums[2]);
    assertEquals(0, nums[3]);
    assertEquals(0, nums[4]);
  }

  @Test
  void moveZeroes2() {
    int[] nums = new int[] {0};
    moveZeroes.moveZeroes(nums);
    assertEquals(0, nums[0]);
  }
}
