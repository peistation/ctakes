/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.dependency.parser.ae;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.core.util.ListFactory;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.Predicate;
import org.apache.ctakes.typesystem.type.textsem.SemanticArgument;
import org.apache.ctakes.typesystem.type.textsem.SemanticRoleRelation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPLib;

import com.googlecode.clearnlp.reader.AbstractReader;

/**
 *This class provides a UIMA wrapper for the ClearNLP Semantic Role Labeler, which is
 * available here.
 * <p>
 * http://code.google.com/p/clearnlp
 * <p>
 * Before using this AnalysisEngine, you should run a Tokenizer, POS-tagger, Lemmatizer, and the
 * CLEAR parser dependency parser.
 * <p>
 * Please see /ClearNLP-wrapper/resources/dependency/clear/README for
 * important information pertaining to the models provided for this parser.
 * <p>
 * 
 */
@TypeCapability(
		inputs = { 
				"org.apache.ctakes.typesystem.type.syntax.BaseToken:partOfSpeech",
				"org.apache.ctakes.typesystem.type.syntax.BaseToken:tokenNumber",
				"org.apache.ctakes.typesystem.type.syntax.BaseToken:end",
				"org.apache.ctakes.typesystem.type.syntax.BaseToken:begin",
				"org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode"
		})
public class ClearNLPSemanticRoleLabelerAE extends JCasAnnotator_ImplBase {
final String language = AbstractReader.LANG_EN;	
  public Logger logger = Logger.getLogger(getClass().getName());
  public static final String DEFAULT_SRL_MODEL_FILE_NAME = "org/apache/ctakes/dependency/parser/models/srl/mayo-en-srl-1.3.0.jar";
  public static final String DEFAULT_PRED_MODEL_FILE_NAME = "org/apache/ctakes/dependency/parser/models/pred/mayo-en-pred-1.3.0.jar";
  public static final String DEFAULT_ROLE_MODEL_FILE_NAME = "org/apache/ctakes/dependency/parser/models/role/mayo-en-role-1.3.0.jar";

  
  public static final String PARAM_PARSER_MODEL_FILE_NAME = "ParserModelFileName";
  public static final String PARAM_PRED_MODEL_FILE_NAME = "ParserPredFileName";
  public static final String PARAM_ROLE_MODEL_FILE_NAME = "ParserRoleFileName";
  
  protected URI srlModelUri;
  protected URI srlPredUri;
  protected URI srlRoleUri;  
	

  @ConfigurationParameter(
		  name = PARAM_PARSER_MODEL_FILE_NAME,
		  description = "This parameter provides the file name of the semantic role labeler model required by the factory method provided by ClearNLPUtil.")
  private String parserModelFileName;

  public static final String PARAM_LEMMATIZER_DATA_FILE = "LemmatizerDataFile";
  
  @ConfigurationParameter(
		  name = PARAM_PRED_MODEL_FILE_NAME,
		  description = "This parameter provides the file name of the semantic role labeler model required by the factory method provided by ClearNLPUtil.")
  private String parserPredFileName;

  @ConfigurationParameter(
		  name = PARAM_ROLE_MODEL_FILE_NAME,
		  description = "This parameter provides the file name of the semantic role labeler model required by the factory method provided by ClearNLPUtil.")
  private String parserRoleFileName;

  
  protected AbstractComponent srlabeler;
  protected AbstractComponent identifier;  
  protected AbstractComponent classifier;


  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    
    try {
    	
    	URL srlPredURL = (this.srlPredUri == null)
                ? this.getClass().getClassLoader().getResource(DEFAULT_PRED_MODEL_FILE_NAME).toURI().toURL()
                : this.srlPredUri.toURL();
        this.identifier = EngineGetter.getComponent(srlPredURL.openStream(), this.language, NLPLib.MODE_PRED);
        
    	URL srlRoleURL = (this.srlRoleUri == null)
                ? this.getClass().getClassLoader().getResource(DEFAULT_ROLE_MODEL_FILE_NAME).toURI().toURL()
                : this.srlRoleUri.toURL();
        this.classifier = EngineGetter.getComponent(srlRoleURL.openStream(), this.language, NLPLib.MODE_ROLE);
    	
    	URL srlModelURL = (this.srlModelUri == null)
                ? this.getClass().getClassLoader().getResource(DEFAULT_SRL_MODEL_FILE_NAME).toURI().toURL()
                : this.srlModelUri.toURL();
        this.srlabeler = EngineGetter.getComponent(srlModelURL.openStream(), this.language, NLPLib.MODE_SRL);       		 

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);
      DEPTree tree = new DEPTree();

      // Build map between CAS dependency node and id for later creation of
      // ClearNLP dependency node/tree
      Map<ConllDependencyNode, Integer> depNodeToID = new HashMap<ConllDependencyNode, Integer>();
      int nodeId = 1;
      for (ConllDependencyNode depNode : JCasUtil.selectCovered(jCas, ConllDependencyNode.class, sentence)) {
        //if (depNode instanceof TopDependencyNode) {
    	if (depNode.getHead() == null) {
    	  // A node without the head is the head of the sentence
          depNodeToID.put(depNode, 0);
        } else {
          depNodeToID.put(depNode, nodeId);
          nodeId++;
        }
      }
      
      int[] headIDs = new int[tokens.size()];
      String[] deprels = new String[tokens.size()];

      // Initialize Token / Sentence info for the ClearNLP Semantic Role Labeler
      for (int i = 0; i < tokens.size(); i++) {
        BaseToken token = tokens.get(i);

        // Determine HeadId
        ConllDependencyNode casDepNode = JCasUtil.selectCovered(jCas, ConllDependencyNode.class, token).get(0);
        deprels[i] = casDepNode.getDeprel();
        ConllDependencyNode head = casDepNode.getHead();

        // If there is no head, this is the head node, set node to 0
        headIDs[i] = (head == null) ? 0 : depNodeToID.get(head);

        // Populate Dependency Node / Tree information
        int id = i + 1;
        String form = casDepNode.getForm();
        String pos = casDepNode.getPostag();
        String lemma = casDepNode.getLemma();
        
        DEPNode node = new DEPNode(id, form, lemma, pos, new DEPFeat());
        tree.add(node);
      }

      for (int i=1; i<tree.size(); i++)
      {
    	  DEPNode node = tree.get(i);
    	  DEPNode head = tree.get(headIDs[i-1]);
    	  String  label = deprels[i-1];
    	  node.setHead(head, label);
      }
      
      tree.initSHeads();
      
      // Run the SRL
      identifier.process(tree);
      classifier.process(tree);
      srlabeler.process(tree);


      // Convert ClearNLP SRL output to CAS types
      extractSRLInfo(jCas, tokens, tree);
      
      
    }
        
  }

  /**
   * Converts the output from the ClearNLP Semantic Role Labeler to the ClearTK Predicate and
   * SemanticArgument Types.
   * 
   * @param jCas
   * @param tokens
   *          - In order list of tokens
   * @param tree
   *          - DepdendencyTree output by ClearNLP SRLPredict
   */
  private void extractSRLInfo(JCas jCas, List<BaseToken> tokens, DEPTree tree) {
    Map<DEPNode, Predicate> headIdToPredicate = new HashMap<DEPNode, Predicate>();
    Map<Predicate, List<SemanticArgument>> predicateArguments = new HashMap<Predicate, List<SemanticArgument>>();

    for (int i = 1; i < tree.size(); i++) {
        // Every ClearNLP parserNode will contain an srlInfo field.
        DEPNode parserNode = tree.get(i);
        BaseToken token = tokens.get(i - 1);
        String rolesetId;
        
        if ((rolesetId = parserNode.getFeat(DEPLib.FEAT_PB)) != null)
        {
          if (!headIdToPredicate.containsKey(parserNode)) {
            // We have not encountered this predicate yet, so create it
            Predicate pred = this.createPredicate(jCas, rolesetId, token);
            headIdToPredicate.put(parserNode, pred);
            pred.setRelations(new EmptyFSList(jCas));
          }
        }
    }
    
    
    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearNLP parserNode will contain an srlInfo field.
      DEPNode parserNode = tree.get(i);
      BaseToken token = tokens.get(i - 1);
     
      for (DEPArc head : parserNode.getSHeads()) {
          Predicate predicate = headIdToPredicate.get(head.getNode());

          // Append this argument to the predicate's list of arguments
          if (!predicateArguments.containsKey(predicate)) {
            predicateArguments.put(predicate, new ArrayList<SemanticArgument>());
          }
          List<SemanticArgument> argumentList = predicateArguments.get(predicate);

          // Create the semantic argument and store for later link creation
          SemanticArgument argument = createArgument(jCas, head, token);
          argumentList.add(argument);
        }     
   }

    // Create relations between predicates and arguments
    for (Map.Entry<Predicate, List<SemanticArgument>> entry : predicateArguments.entrySet()) {
      Predicate predicate = entry.getKey();
      
      List<SemanticRoleRelation> relations = new ArrayList<SemanticRoleRelation>();
      for (SemanticArgument argument : entry.getValue()) {
    	  SemanticRoleRelation relation = new SemanticRoleRelation(jCas);
    	  relation.setArgument(argument);
    	  relation.setPredicate(predicate);
    	  relation.setCategory(argument.getLabel());
    	  relation.addToIndexes();
    	  relations.add(relation);
    	  argument.setRelation(relation);
      }
      
      FSList relationsList = ListFactory.buildList(jCas, relations.toArray(new TOP[relations.size()]));
      predicate.setRelations(relationsList);
    }
  }

  private Predicate createPredicate(JCas jCas, String rolesetId, BaseToken token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  private SemanticArgument createArgument(JCas jCas, DEPArc head, BaseToken token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.getLabel());
    argument.addToIndexes();
    return argument;
  }

}
