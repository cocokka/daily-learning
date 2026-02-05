package com.demo.dl.algorithm.problems;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/container-with-most-water/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class ContainerWithMostWater {
  public int maxArea(int[] heights) {
    int left = 0;
    int right = heights.length - 1;
    int maxArea = 0;
    while (left < right) {
      int length = right - left;
      int height = Math.min(heights[left], heights[right]);
      int area = length * height;
      maxArea = Math.max(maxArea, area);
      if (heights[left] < heights[right]) {
        left++;
      } else {
        right--;
      }
    }
    return maxArea;
  }
}
