package com.demo.dl.algorithm.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/3sum/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class ThreeSum {

  public List<List<Integer>> threeSum(Integer[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> results = new ArrayList<>();

    for (int i = 0; i < nums.length; i++) {

      if (nums[i] > 0) {
        break;
      }
      // skip duplicate
      if (i > 0 && nums[i].equals(nums[i - 1])) {
        continue;
      }

      twoSum(nums, i, results);
    }

    return results;
  }

  private static void twoSum(Integer[] nums, int i, List<List<Integer>> results) {
    int next = i + 1;
    int right = nums.length - 1;
    Integer target = -nums[i];

    while (next < right) {
      Integer sum = nums[next] + nums[right];
      if (target.equals(sum)) {
        results.add(List.of(nums[i], nums[next], nums[right]));
        next++;
        right--;

        // skip duplicate
        while (next < right && nums[next].equals(nums[next - 1])) {
          next++;
        }
        while (next < right && nums[right].equals(nums[right + 1])) {
          right--;
        }
      } else if (target > sum) {
        next++;
      } else {
        right--;
      }
    }
  }
}
