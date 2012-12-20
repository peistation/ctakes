package org.apache.ctakes.temporal.eval;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for splitting the THYME data into appropriate sets for evaluation.
 */
public class THYMEData {

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
