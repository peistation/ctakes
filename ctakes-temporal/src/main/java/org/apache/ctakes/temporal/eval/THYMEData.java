package org.apache.ctakes.temporal.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * A class for splitting the THYME data into appropriate sets for evaluation.
 */
public class THYMEData {
  public static final Set<String> SEGMENTS_TO_SKIP = Sets.newHashSet("20104", "20105", "20116", "20138");

  public static List<Integer> getTrainPatientSets(List<Integer> patientSets) {
    List<Integer> items = new ArrayList<Integer>();
    for (Integer i : patientSets) {
      int remainder = i % 8;
      if (remainder < 4) {
        items.add(i);
      }
    }
    return items;
  }

  public static List<Integer> getDevPatientSets(List<Integer> patientSets) {
    List<Integer> items = new ArrayList<Integer>();
    for (Integer i : patientSets) {
      int remainder = i % 8;
      if (4 <= remainder && remainder < 6) {
        items.add(i);
      }
    }
    return items;
  }

  public static List<Integer> getTestPatientSets(List<Integer> patientSets) {
    List<Integer> items = new ArrayList<Integer>();
    for (Integer i : patientSets) {
      int remainder = i % 8;
      if (6 <= remainder) {
        items.add(i);
      }
    }
    return items;
  }
}
