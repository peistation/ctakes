package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.CollectionReaderFactory;

public class XMICorpusReaderProvider extends CorpusReaderProvider_ImplBase<File> {

  private TypeSystemDescription typeSystemDescription;

  public XMICorpusReaderProvider(
      TypeSystemDescription typeSystemDescription,
      List<File> trainFiles,
      List<File> testFiles) {
    super(trainFiles, testFiles);
    this.typeSystemDescription = typeSystemDescription;
  }

  @Override
  protected CollectionReader getReader(List<File> items) throws UIMAException {
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        this.typeSystemDescription,
        XMIReader.PARAM_FILES,
        paths);
  }

}
