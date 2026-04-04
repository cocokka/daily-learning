package com.demo.dl.algorithm.problems;

/** <a href="https://leetcode.cn/problems/single-number-ii">...</a> */
public class SingleNumber2 {

  static void main() {
    int[] nums = new int[] {1, 2, 2, 2};
    System.out.println(singleNumber(nums));
  }

  public static int singleNumber(int[] nums) {

    int ones = 0;
    int twos = 0;
    for (int num : nums) {
      ones = ones ^ num & ~twos;
      twos = twos ^ num & ~ones;
    }
    return ones;
  }
}
