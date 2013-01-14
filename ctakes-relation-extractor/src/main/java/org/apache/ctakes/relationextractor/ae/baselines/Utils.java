package org.apache.ctakes.relationextractor.ae.baselines;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator.IdentifiedAnnotationPair;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

public class Utils {

  /**
   * Are entity types of the arguments valid for location_of relation? 
   * The following combinations are allowed:
   * 
   * location-of(anatomical site/6, disorder/2)
   * location-of(anatomical site/6, sign/symptom/3)
   * location-of(anatomical site/6, procedure/5)
   */
  public static boolean validateLocationOfArgumentTypes(IdentifiedAnnotationPair pair) {
    
    // allowable arg2 types for location_of
    HashSet<Integer> okArg2Types = new HashSet<Integer>(Arrays.asList(2, 3, 5));
    
    IdentifiedAnnotation arg1 = pair.getArg1(); // Argument (should be anatomical site)
    IdentifiedAnnotation arg2 = pair.getArg2(); // Related_to (should be either disorder, sign/symptom, or procedure)
    int type1 = arg1.getTypeID();
    int type2 = arg2.getTypeID();
    
    if(type1 == 6 && okArg2Types.contains(type2)) {
      return true;
    }
    
    return false;
  }

  /**
   * Are entity types of the arguments valid for degree_of relation? 
   * The following are the valid combinations:
   * 
   * degree-of(disorder/2, modifier)
   * degree-of(sign/symptom/3, modifier)
   */
  public static boolean validateDegreeOfArgumentTypes(IdentifiedAnnotationPair pair) {

    // allowable arg1 types
    HashSet<Integer> okArg1Types = new HashSet<Integer>(Arrays.asList(2, 3));

    IdentifiedAnnotation arg1 = pair.getArg1(); // Argument (should be either disease/disorder or sign/symptom
    IdentifiedAnnotation arg2 = pair.getArg2(); // Related_to (should be a modifier)
    int type1 = arg1.getTypeID();
    int type2 = arg2.getTypeID();

    if(okArg1Types.contains(type1)) {
      return true; // assume arg2 is a moddifier
    }

    return false;
  }
}
