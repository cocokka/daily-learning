package com.demo.dl.algorithm.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupAnagramsTest {

  GroupAnagrams groupAnagrams;

  @BeforeEach
  void setUp() {
    groupAnagrams = new GroupAnagrams();
  }

  @Test
  void groupAnagrams1() {
    String[] strs = new String[] {"eat", "tea", "tan", "ate", "nat", "bat"};
    List<List<String>> results = groupAnagrams.groupAnagrams(strs);
    assertEquals(3, results.size());
    printOutput(results); // [["bat"],["nat","tan"],["ate","eat","tea"]]
  }

  @Test
  void groupAnagrams2() {
    String[] strs = new String[] {""};
    List<List<String>> results = groupAnagrams.groupAnagrams(strs);
    assertEquals(1, results.size());
    printOutput(results); // [[""]]
  }

  @Test
  void groupAnagrams3() {
    String[] strs = new String[] {"a"};
    List<List<String>> results = groupAnagrams.groupAnagrams(strs);
    assertEquals(1, results.size());
    printOutput(results); // [["a"]]
  }

  private void printOutput(List<List<String>> results) {
    System.out.print("Output is: [");
    for (int i = 0; i < results.size(); i++) {
      List<String> group = results.get(i);
      System.out.print("[");
      for (int j = 0; j < group.size(); j++) {
        System.out.print("\"" + group.get(j) + "\"");
        if (j < group.size() - 1) {
          System.out.print(",");
        }
      }
      System.out.print("]");
      if (i < results.size() - 1) {
        System.out.print(",");
      }
    }
    System.out.print("]");
  }
}
