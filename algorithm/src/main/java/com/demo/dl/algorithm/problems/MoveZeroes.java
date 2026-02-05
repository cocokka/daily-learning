package com.demo.dl.algorithm.problems;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/move-zeroes/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class MoveZeroes {

  public void moveZeroes(int[] nums) {
    if (nums != null) {
      int slower = 0;
      int faster = 0;
      while (faster < nums.length) {
        if (nums[faster] != 0) {
          int temp = nums[slower];
          nums[slower] = nums[faster];
          nums[faster] = temp;
          slower++;
        }
        faster++;
      }
    }
  }
}
