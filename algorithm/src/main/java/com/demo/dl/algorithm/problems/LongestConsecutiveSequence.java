package com.demo.dl.algorithm.problems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/longest-consecutive-sequence/description/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class LongestConsecutiveSequence {

  public Integer longestConsecutive(Integer[] nums) {
    // distinct array
    Set<Integer> numbers = HashSet.newHashSet(nums.length);
    numbers.addAll(Arrays.asList(nums));

    int size = numbers.size();
    int maxLength = 0;
    for (Integer number : numbers) {
      Integer left = number - 1;
      Integer right = number + 1;
      if (!numbers.contains(left)) {
        // first node
        while (numbers.contains(right)) {
          right++;
        }
        int currentLength = right - number;
        maxLength = Math.max(maxLength, currentLength);
        // Since the elements in the collection are unique, when the current length exceeds half,
        // there will not be any greater length than it. At this point, we can return the current
        // maximum length immediately.
        if (maxLength * 2 > size) {
          break;
        }
      }
    }

    return maxLength;
  }
}
