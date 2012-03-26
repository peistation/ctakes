package org.mitre.medfacts.uima.assertion;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class CasIndexer<T extends Annotation>
{
  private Logger logger = Logger.getLogger(CasIndexer.class.getName());
  private JCas jcas;
  protected Map<Integer, T> mapByAddress;
  protected Type targetType;
  
  public CasIndexer(JCas jcas, Type targetType)
  {
    this.jcas = jcas;
    this.targetType = targetType;
    initialize();
  }
  
  public CasIndexer()
  {
  }
  
  public void initialize()
  {
    AnnotationIndex<Annotation> annotationIndex = null;
    if (targetType == null)
    {
      annotationIndex = jcas.getAnnotationIndex();
    } else
    {
      annotationIndex = jcas.getAnnotationIndex(targetType);
    }
    
    mapByAddress = new HashMap<Integer, T>();
    
    logger.info("    before iterating over all annotations in index...");
    for (Annotation annotation : annotationIndex)
    {
      logger.info("    begin single annotation");
      Integer address = annotation.getAddress();
      logger.info(String.format("      address: %d; type: %s", address, annotation.getClass().getName()));
      T current = (T)annotation;
      
      mapByAddress.put(address,  current);
      logger.info("    end single annotation");
    }
    logger.info("    after iterating over all annotations in index...");
    
  }
  
  public Annotation lookupByAddress(int address)
  {
    return mapByAddress.get(address);
  }
  
  public String convertToDebugOutput(String label, T targetAnnotation)
  {
    StringWriter sw = new StringWriter();
    PrintWriter p = new PrintWriter(sw);
    
    if (label == null) label = "";
    
    p.format("=== \"%s\" BEGIN [%d] ===%n", label, targetAnnotation.getAddress());
    
    Set<Integer> addressSet = new TreeSet<Integer>();
    if (mapByAddress != null)
    {
      addressSet.addAll(mapByAddress.keySet());
    }
    
    if (!addressSet.isEmpty())
    {
      for (Integer currentAddress : addressSet)
      {
        String highlightIfMatching = currentAddress.equals(targetAnnotation.getAddress()) ? "###" : "";
        p.format("ANNOTATION: address: %d %s%n", currentAddress, highlightIfMatching);
        T currentAnnotation = mapByAddress.get(currentAddress);
        if (currentAnnotation == null)
        {
          p.println("  annotation IS NULL");
        } else
        {
          p.format("  class: %s%n", currentAnnotation.getClass().getName());
          p.format("  value: %s%n", currentAnnotation.toString());
        }
      }
    }
    
    p.format("=== \"%s\" END [%d] ===%n", label, targetAnnotation.getAddress());
    
    String output = sw.toString();
    p.close();
    
    return output;
  }

  public JCas getJcas()
  {
    return jcas;
  }

  public void setJcas(JCas jcas)
  {
    this.jcas = jcas;
  }
}
