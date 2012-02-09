package org.chboston.cnlp.ctakes.coref.uima.cc;

/**
 * Represents a Knowtator Project. The only public constructor takes a string
 * parameter representing a protege-project-file-name to initialize instance
 * variables.
 *
 * Intended use: Extend this class to implement the processes that you may want to
 * apply to the loaded knowtator project.
 * @author Vinod C Kaggal
 *
 */
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.TextSourceIterator;
import edu.uchsc.ccp.knowtator.textsource.files.FileTextSourceCollection;

public class KnowtatorProject
{
	//--public Knowtator Project ----
	public KnowtatorProject(String protegeProjectFile, String textSourceDirName)
	throws TextSourceAccessException, IOException
	{
		errors = new ArrayList();

		//load project
		protegeProject = new Project(protegeProjectFile, errors);
		//access knowledgebase
		kb = protegeProject.getKnowledgeBase();
		//create ProjectUtil
		kpu = new KnowtatorProjectUtil(kb);

		KnowtatorManager km = new KnowtatorManager(kpu);
		annotationUtil = new AnnotationUtil(km);

		tsu = new TextSourceUtil(annotationUtil, kpu);

		tsc = new FileTextSourceCollection(textSourceDirName, Charset.defaultCharset());
		tscItr = tsc.iterator();

		tsu.setCurrentTextSourceCollection(tsc);
		annotationUtil.setTextSourceUtil(tsu);
		mentionUtil = new MentionUtil(kpu);
		annotationUtil.setMentionUtil(mentionUtil);

	}

	//-- utility methods -----
	public void next()
	throws TextSourceAccessException
	{
		textDoc = tscItr.next();
	}

	public boolean hasNextDoc()
	{ return tscItr.hasNext(); }

	public TextSource getNextTextSource()
	throws TextSourceAccessException
	{ return textDoc;  }

	public String getNextTextSourceString()
	throws TextSourceAccessException
	{ return textDoc.getText();  }

	//-- protected data members ------
	protected List errors;
	protected Project protegeProject;
	protected TextSourceCollection tsc;
	protected TextSourceIterator tscItr;
	protected TextSource textDoc;
	protected KnowledgeBase kb;
	protected KnowtatorProjectUtil kpu;
	protected AnnotationUtil annotationUtil;
	protected MentionUtil mentionUtil;
	protected SimpleInstance annotator;
	protected TextSourceUtil tsu;
}
