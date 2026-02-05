package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContainerWithMostWaterTest {
  ContainerWithMostWater containerWithMostWater;

  @BeforeEach
  void setUp() {

    containerWithMostWater = new ContainerWithMostWater();
  }

  @Test
  void maxArea1() {
    int[] heights = new int[] {1, 8, 6, 2, 5, 4, 8, 3, 7};
    int maxArea = containerWithMostWater.maxArea(heights);
    assertEquals(49, maxArea);
  }

  @Test
  void maxArea2() {
    int[] heights = new int[] {1, 1};
    int maxArea = containerWithMostWater.maxArea(heights);
    assertEquals(1, maxArea);
  }
}
