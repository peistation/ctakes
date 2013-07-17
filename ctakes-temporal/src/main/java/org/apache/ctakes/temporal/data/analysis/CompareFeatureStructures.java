package org.apache.ctakes.temporal.data.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.factory.JCasFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class CompareFeatureStructures {
  static interface Options {
    @Option(longName = "dir1")
    public File getDirectory1();

    @Option(longName = "dir2")
    public File getDirectory2();

    @Option(longName = "roots", defaultValue = {
        "org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation",
        "org.apache.ctakes.typesystem.type.relation.Relation" })
    public List<String> getAnnotationClassNames();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Class<?>> annotationClasses = Lists.newArrayList();
    for (String annotationClassName : options.getAnnotationClassNames()) {
      annotationClasses.add(Class.forName(annotationClassName));
    }
    File dir1 = options.getDirectory1();
    File dir2 = options.getDirectory2();
    if (!Arrays.equals(dir1.list(), dir2.list())) {
      System.err.printf("%s and %s contain different files", dir1, dir2);
    } else {
      for (String fileName : dir1.list()) {
        System.err.printf("== Checking %s ===\n", fileName);
        JCas jCas1 = readXMI(new File(dir1, fileName));
        JCas jCas2 = readXMI(new File(dir2, fileName));
        List<String> viewNames1 = getViewNames(jCas1);
        List<String> viewNames2 = getViewNames(jCas2);
        if (areEqual("view-names", viewNames1, viewNames2)) {
          for (String viewName : viewNames1) {
            JCas view1 = jCas1.getView(viewName);
            JCas view2 = jCas2.getView(viewName);
            for (Class<?> annotationClass : annotationClasses) {
              Multimap<Type, FeatureStructure> fsMap1 = toSortedMultimap(view1, annotationClass);
              Multimap<Type, FeatureStructure> fsMap2 = toSortedMultimap(view2, annotationClass);
              if (areEqual("annotation-counts", fsMap1.keys(), fsMap2.keys())) {
                for (Type type : fsMap1.keySet()) {
                  Iterator<FeatureStructure> fsIter1 = fsMap1.get(type).iterator();
                  Iterator<FeatureStructure> fsIter2 = fsMap2.get(type).iterator();
                  while (fsIter1.hasNext() && fsIter2.hasNext()) {
                    FeatureStructure fs1 = fsIter1.next();
                    FeatureStructure fs2 = fsIter2.next();
                    FSDiff diff = new FSDiff(fs1, fs2);
                    if (diff.hasDifferences()) {
                      System.err.println(diff);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

  }

  private static JCas readXMI(File xmiFile) throws Exception {
    JCas jCas = JCasFactory.createJCas();
    FileInputStream inputStream = new FileInputStream(xmiFile);
    try {
      XmiCasDeserializer.deserialize(inputStream, jCas.getCas());
    } finally {
      inputStream.close();
    }
    return jCas;
  }

  private static List<String> getViewNames(JCas jCas) throws CASException {
    List<String> names = Lists.newArrayList();
    Iterator<JCas> views = jCas.getViewIterator();
    while (views.hasNext()) {
      names.add(views.next().getViewName());
    }
    return names;
  }

  private static boolean areEqual(String name, Object o1, Object o2) {
    boolean areEqual = Objects.equal(o1, o2);
    if (!areEqual) {
      System.err.printf("Difference in %s:\n-%s\n+%s\n", name, o1, o2);
    }
    return areEqual;
  }

  private static Multimap<Type, FeatureStructure> toSortedMultimap(
      JCas jCas,
      Class<?> annotationClass) {
    Type type = JCasUtil.getType(jCas, annotationClass);
    FSIterator<FeatureStructure> fsIterator = jCas.getFSIndexRepository().getAllIndexedFS(type);
    Multimap<Type, FeatureStructure> result = TreeMultimap.create(BY_NAME, BY_OFFSETS);
    while (fsIterator.hasNext()) {
      FeatureStructure fs = fsIterator.next();
      result.put(fs.getType(), fs);
    }
    return result;
  }
  
  private static final Ordering<Type> BY_NAME = Ordering.natural().onResultOf(
      new Function<Type, String>() {
        @Override
        public String apply(@Nullable Type input) {
          return input.getName();
        }
      });

  private static final Ordering<FeatureStructure> BY_OFFSETS =
      Ordering.natural().<Integer> lexicographical().onResultOf(
          new Function<FeatureStructure, Iterable<Integer>>() {
            @Override
            public Iterable<Integer> apply(@Nullable FeatureStructure input) {
              List<Integer> offsets = Lists.newArrayList();
              if (input != null) {
                if (input instanceof Annotation) {
                  Annotation annotation = (Annotation) input;
                  offsets.add(annotation.getBegin());
                  offsets.add(annotation.getEnd());
                } else if (input instanceof FSArray) {
                  FSArray fsArray = (FSArray) input;
                  for (int i = 0; i < fsArray.size(); ++i) {
                    Iterables.addAll(offsets, this.apply(fsArray.get(i)));
                  }
                } else if (input instanceof NonEmptyFSList) {
                  NonEmptyFSList fsList = (NonEmptyFSList) input;
                  Iterables.addAll(offsets, this.apply(fsList.getHead()));
                  Iterables.addAll(offsets, this.apply(fsList.getTail()));
                } else {
                  for (Feature feature : input.getType().getFeatures()) {
                    if (!feature.getRange().isPrimitive()) {
                      Iterables.addAll(offsets, this.apply(input.getFeatureValue(feature)));
                    }
                  }
                }
              }
              return offsets;
            }
          });

  public static class FSDiff {
    private List<FSDifference> differences;
    private FeatureStructure root1, root2;

    public FSDiff(FeatureStructure root1, FeatureStructure root2) {
      this.root1 = root1;
      this.root2 = root2;
      this.differences = Lists.newArrayList();
      this.findDifferences(
          this.root1,
          this.root2,
          Lists.<Feature> newArrayList(),
          Lists.<FeatureStructure> newArrayList());
    }

    public boolean hasDifferences() {
      return !this.differences.isEmpty();
    }

    @Override
    public String toString() {
      String diff;
      if (!this.hasDifferences()) {
        diff = "";
      } else {
        List<String> paths = Lists.newArrayList();
        for (FSDifference difference : this.differences) {
          List<String> featureNames = Lists.newArrayList();
          for (Feature feature : difference.getPath()) {
            featureNames.add(feature.getShortName());
          }
          paths.add(Joiner.on('/').join(featureNames));
        }
        diff = this.root1.toString();
        for (FSDifference difference : this.differences) {
          String value1 = difference.getValue1().toString().trim();
          String value2 = difference.getValue2().toString().trim();
          String value1space = value1.replaceAll("\\s+", "\\\\s+");
          Pattern pattern =
              Pattern.compile(String.format("^(.*?)(%s)", value1space), Pattern.MULTILINE);
          Matcher matcher = pattern.matcher(diff);
          StringBuffer buffer = new StringBuffer();
          while (matcher.find()) {
            String prefix = matcher.group(1);
            String replacement;
            // don't re-replace things that have already been taken care of
            if (prefix.startsWith("-") || prefix.startsWith("+")) {
              replacement = matcher.group();
            }
            // replace the current text with diff-style +/- text
            else {
              Matcher indentMatcher = Pattern.compile("^\\s*").matcher(prefix);
              indentMatcher.find();
              String indent = indentMatcher.group();
              replacement =
                  String.format(
                      "%s%s\n%s%s",
                      "-" + prefix,
                      value1.replaceAll("\n", "\n-" + indent),
                      "+" + prefix,
                      value2.replaceAll("\n", "\n+" + indent));
            }
            matcher.appendReplacement(buffer, replacement);
          }
          matcher.appendTail(buffer);
          diff = buffer.toString();
        }
        diff = diff.replaceAll("(?m)^(?![+-])", " ");
        diff = String.format("Difference in %s:\n%s", paths, diff);
      }
      return diff;
    }

    private void findDifferences(
        FeatureStructure fs1,
        FeatureStructure fs2,
        List<Feature> featurePath,
        List<FeatureStructure> seen) {
      if (!seen.contains(fs1) && !seen.contains(fs2)) {
        seen.add(fs1);
        seen.add(fs2);
        for (Feature feature : fs1.getType().getFeatures()) {
          if (feature.getName().equals("uima.cas.AnnotationBase:sofa")) {
            continue;
          }
          List<Feature> newPath = Lists.newArrayList(featurePath);
          newPath.add(feature);
          if (feature.getRange().isPrimitive()) {
            String value1 = fs1.getFeatureValueAsString(feature);
            String value2 = fs2.getFeatureValueAsString(feature);
            if (!Objects.equal(value1, value2)) {
              this.differences.add(new FSDifference(newPath, value1, value2));
            }
          } else {
            FeatureStructure value1 = fs1.getFeatureValue(feature);
            FeatureStructure value2 = fs2.getFeatureValue(feature);
            if (value1 == null
                || value2 == null
                || !value1.getType().getName().equals(value2.getType().getName())) {
              if (!Objects.equal(value1, value2)) {
                this.differences.add(new FSDifference(newPath, value1, value2));
              }
            } else {
              this.findDifferences(value1, value2, newPath, seen);
            }
          }
        }
      }
    }
  }

  public static class FSDifference {

    private List<Feature> path;
    private Object value1, value2;

    public FSDifference(List<Feature> path, Object value1, Object value2) {
      this.path = path;
      this.value1 = value1;
      this.value2 = value2;
    }

    public List<Feature> getPath() {
      return path;
    }

    public Object getValue1() {
      return value1;
    }

    public Object getValue2() {
      return value2;
    }
  }
}
