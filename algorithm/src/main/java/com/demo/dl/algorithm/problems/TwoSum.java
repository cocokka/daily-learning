package com.demo.dl.algorithm.problems;

import java.util.HashMap;
import java.util.Map;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/two-sum/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class TwoSum {

  /**
   * Only loop once, use a map to record each value and corresponding position in the array, and
   * query whether there is an inch in the map by the difference from the target value.
   *
   * @param nums input numbers as an array.
   * @param target the value of the sum of two numbers.
   * @return two positions in the array.
   */
  public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = HashMap.newHashMap(nums.length);
    for (int i = 0; i < nums.length; i++) {
      int complement = target - nums[i];
      if (map.containsKey(complement)) {
        return new int[] {map.get(complement), i};
      } else {
        map.put(nums[i], i);
      }
    }
    return new int[] {-1, -1};
  }
}
