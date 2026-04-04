package com.demo.dl.algorithm.problems;

import java.util.HashMap;
import java.util.Map;

/** https://leetcode.cn/problems/single-number/ */
public class SingleNumber {

  static void main() {
    Integer[] nums1 = new Integer[] {2, 2, 1};
    System.out.println(singleNumber(nums1));

    Integer[] nums2 = new Integer[] {4, 1, 2, 1, 2};
    System.out.println(singleNumber(nums2));

    Integer[] nums3 = new Integer[] {1};
    System.out.println(singleNumber(nums3));
  }

  public static Integer singleNumber(Integer[] nums) {
    Map<Integer, Integer> result = new HashMap<>(nums.length);
    for (Integer num : nums) {
      if (result.containsKey(num)) {
        result.put(num, result.get(num) + 1);
      } else {
        result.put(num, 1);
      }
    }
    for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
      if (entry.getValue().equals(Integer.valueOf(1))) {
        return entry.getKey();
      }
    }
    return 0;
  }

  /**
   * 异或运算的核心特性： 任何数和自身异或，结果为0：a ^ a = 0 任何数和0异或，结果为自身：a ^ 0 = a 异或运算满足交换律和结合律：a ^ b ^ a = b ^ (a ^
   * a) = b ^ 0 = b
   *
   * @param nums
   * @return
   */
  public static int singleNumber(int[] nums) {
    int result = 0;
    for (int num : nums) {
      result = num ^ result;
    }
    return result;
  }
}
