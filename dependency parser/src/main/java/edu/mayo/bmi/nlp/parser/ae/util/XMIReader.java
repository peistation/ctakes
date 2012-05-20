package edu.mayo.bmi.nlp.parser.ae.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.xml.sax.SAXException;


/**
 * UIMA CollectionReader that reads in CASes from XMI files.
 */
public class XMIReader extends JCasCollectionReader_ImplBase {

	public static final String PARAM_FILES = "files";

	@ConfigurationParameter(
			name = PARAM_FILES,
			mandatory = true,
			description = "The XMI files to be loaded")
	private List<File> files;

	private Iterator<File> filesIter;

	private int completed;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.filesIter = files.iterator();
		this.completed = 0;
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(
				this.completed,
				this.files.size(),
				Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return this.filesIter.hasNext();
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		FileInputStream inputStream = new FileInputStream(this.filesIter.next());
		try {
			XmiCasDeserializer.deserialize(new BufferedInputStream(inputStream), jCas.getCas());
		} catch (SAXException e) {
			throw new CollectionException(e);
		}
		inputStream.close();
		this.completed += 1;
	}

}
