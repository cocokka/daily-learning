package com.demo.dl.algorithm.problems;

import java.util.*;

/**
 * @link <a
 *     href="https://leetcode.cn/problems/group-anagrams/?envType=study-plan-v2&envId=top-100-liked">...</a>
 */
public class GroupAnagrams {

  /**
   * Sort every strings by the same order, if the values are the same, it means these strings have
   * same letters.
   *
   * @param strs an array with strings.
   * @return retrun a collection of words composed of the same letters.
   */
  public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> result = HashMap.newHashMap(strs.length);
    for (String str : strs) {
      char[] chars = str.toCharArray();
      Arrays.sort(chars);
      String key = new String(chars);
      result.putIfAbsent(key, new ArrayList<>());
      result.get(key).add(str);
    }

    return new ArrayList<>(result.values());
  }
}
