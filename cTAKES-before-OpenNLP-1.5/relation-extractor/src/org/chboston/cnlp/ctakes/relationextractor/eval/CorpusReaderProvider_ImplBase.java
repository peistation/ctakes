package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.uimafit.component.NoOpAnnotator;
import org.uimafit.factory.AnalysisEngineFactory;

public abstract class CorpusReaderProvider_ImplBase<ITEM_TYPE> implements CorpusReaderProvider {

  protected List<ITEM_TYPE> trainItems;

  protected List<ITEM_TYPE> testItems;

  protected int numberOfFolds;

  public CorpusReaderProvider_ImplBase(
      List<ITEM_TYPE> trainItems,
      List<ITEM_TYPE> testItems) {
    this.numberOfFolds = Math.min(10, trainItems.size());
    this.trainItems = trainItems;
    this.testItems = testItems;
  }

  protected abstract CollectionReader getReader(List<ITEM_TYPE> items) throws UIMAException;

  @Override
  public AnalysisEngine getPreprocessor() throws UIMAException {
    return AnalysisEngineFactory.createPrimitive(NoOpAnnotator.class);
  }

  @Override
  public int getNumberOfFolds() {
    return this.numberOfFolds;
  }

  @Override
  public void setNumberOfFolds(int numberOfFolds) {
    if (numberOfFolds > this.trainItems.size()) {
      String message = "Expected %d or fewer folds, found %d";
      throw new RuntimeException(String.format(message, this.trainItems.size(), numberOfFolds));
    }
    this.numberOfFolds = numberOfFolds;
  }

  @Override
  public CollectionReader getTrainReader(int fold) throws UIMAException {
    int totalFolds = this.getNumberOfFolds();

    // train items for this fold are all items *except for* every Nth item
    List<ITEM_TYPE> items = new ArrayList<ITEM_TYPE>();
    for (int i = 0; i < this.trainItems.size(); ++i) {
      if (i % totalFolds != fold) {
        items.add(this.trainItems.get(i));
      }
    }
    // create the reader from the items
    return this.getReader(items);
  }

  @Override
  public CollectionReader getTestReader(int fold) throws UIMAException {
    int totalFolds = this.getNumberOfFolds();

    // test items for this fold are every Nth item
    List<ITEM_TYPE> items = new ArrayList<ITEM_TYPE>();
    for (int i = 0; i < this.trainItems.size(); ++i) {
      if (i % totalFolds == fold) {
        items.add(this.trainItems.get(i));
      }
    }
    // create the reader from the items
    return this.getReader(items);
  }

  @Override
  public CollectionReader getTrainReader() throws UIMAException {
    return this.getReader(this.trainItems);
  }

  @Override
  public CollectionReader getTestReader() throws UIMAException {
    return this.getReader(this.testItems);
  }

  @Override
  public CollectionReader getReader() throws UIMAException {
    List<ITEM_TYPE> items = new ArrayList<ITEM_TYPE>();
    items.addAll(this.trainItems);
    items.addAll(this.testItems);
    return this.getReader(items);
  }
}
