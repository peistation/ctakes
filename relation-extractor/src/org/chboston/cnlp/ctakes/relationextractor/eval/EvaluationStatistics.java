package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

class EvaluationStatistics<OUTCOME_TYPE extends Comparable<? super OUTCOME_TYPE>> {

  private Multiset<OUTCOME_TYPE> gold = HashMultiset.create();

  private Multiset<OUTCOME_TYPE> system = HashMultiset.create();

  private Multiset<OUTCOME_TYPE> correct = HashMultiset.create();

  public void update(
      Collection<OUTCOME_TYPE> gold,
      Collection<OUTCOME_TYPE> system,
      Collection<OUTCOME_TYPE> correct) {
    this.gold.addAll(gold);
    this.system.addAll(system);
    this.correct.addAll(correct);
  }

  public double precision() {
    int nSystem = this.system.size();
    return nSystem == 0 ? 1.0 : ((double) this.correct.size()) / nSystem;
  }

  public double precision(OUTCOME_TYPE outcome) {
    int nSystem = this.system.count(outcome);
    return nSystem == 0 ? 1.0 : ((double) this.correct.count(outcome)) / nSystem;
  }

  public double recall() {
    int nGold = this.gold.size();
    return nGold == 0 ? 1.0 : ((double) this.correct.size()) / nGold;
  }

  public double recall(OUTCOME_TYPE outcome) {
    int nGold = this.gold.count(outcome);
    return nGold == 0 ? 1.0 : ((double) this.correct.count(outcome)) / nGold;
  }

  public double f1() {
    double p = this.precision();
    double r = this.recall();
    double sum = p + r;
    return sum == 0.0 ? 0.0 : (2 * p * r) / sum;
  }

  public double f1(OUTCOME_TYPE outcome) {
    double p = this.precision(outcome);
    double r = this.recall(outcome);
    double sum = p + r;
    return sum == 0.0 ? 0.0 : (2 * p * r) / sum;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("\n");
    result.append("P\tR\tF1\t#gold\t#system\t#correct\n");
    result.append(String.format(
        "%.3f\t%.3f\t%.3f\t%d\t%d\t%d\tOVERALL\n",
        this.precision(),
        this.recall(),
        this.f1(),
        this.gold.size(),
        this.system.size(),
        this.correct.size()));
    List<OUTCOME_TYPE> outcomes = new ArrayList<OUTCOME_TYPE>(this.gold.elementSet());
    Collections.sort(outcomes);
    for (OUTCOME_TYPE outcome : outcomes) {
      result.append(String.format(
          "%.3f\t%.3f\t%.3f\t%d\t%d\t%d\t%s\n",
          this.precision(outcome),
          this.recall(outcome),
          this.f1(outcome),
          this.gold.count(outcome),
          this.system.count(outcome),
          this.correct.count(outcome),
          outcome));
    }
    return result.toString();
  }
}
