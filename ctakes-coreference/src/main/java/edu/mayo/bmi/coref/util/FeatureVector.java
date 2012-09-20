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
package edu.mayo.bmi.coref.util;

public class FeatureVector {

	final static String[] anaph_feats = {
			"Definite"         , // {Y,N}
			"Indefinite"       , // {Y,N}
			//"Number"           , // {S, P, U}
			"NumSing"          , // {Y,N}
			"NumPlu"           , // {Y,N}
			"NumUnk"           , // {Y,N}
			//"WnClass"          , // [0-6]
			"IsDrug"           , // {Y,N}
			"IsDisorder"       , // {Y,N}
			"IsFinding"        , // {Y,N}
			"IsProcedure"      , // {Y,N}
			"IsAnatomicalSite" , // {Y,N}
			"ProStr"           , // {Y,N}
			"NPHead"           , // {Y,N}
			"SimilarStr"       , // {Y,N}
	};

	final static String[] ne_coref_feats = {
			"SameSection",  // {yes, no}	 1
			"TokenDistance"   , // numeric    
			"SentenceDistance", // numeric
			"ExactMatch"      , // {yes,no}
			"StartMatch"      , // {yes,no}
//			"MidMatch"        , // {yes,no}
			"EndMatch"        , // {yes,no}
//			"StringMatch"	  , // {yes,no}
			"SoonStr"         , // {C,I}
			"Pronoun1"        , // {Y,N}
			"Pronoun2"        , // {Y,N}
			"Definite2"       , // {Y,N}      10
			"Demonstrative2"  , // {Y,N}      
			//"NumberMatch"     , // {C,I,NA}
			"NumberMatchC"    , // {Y,N}
			"NumberMatchI"    , // {Y,N}
			"NumberMatchNA"   , // {Y,N}
			//"WnClass"         , // {C,I,NA}
			"WnClassC"        , // {Y,N}
			"WnClassI"        , // {Y,N}
			"WnClassNA"       , // {Y,N}
			"Alias"           , // {C,I}
			"ProStr"          , // {C,I}
			"SoonStrNonpro"   , // {C,I}      20
			"WordOverlap"     , // {C,I}
			"WordsSubstr"     , // {C,I}
			//"BothDefinites"   , // {C,I,NA}
			"BothDefinitesC"  , // {Y,N}
			"BothDefinitesI"  , // {Y,N}
			"BothDefinitesNA" , // {Y,N}
			//"BothEmbedded"    , // {C,I,NA}
			"BothEmbeddedC"   , // {Y,N}
			"BothEmbeddedI"   , // {Y,N}
			"BothEmbeddedNA"  , // {Y,N}
			//"BothPronouns"    , // {C,I,NA}
			"BothPronounsC"   , // {Y,N}
			"BothPronounsI"   , // {Y,N}   30
			"BothPronounsNA"  , // {Y,N}
			"Indefinite"      , // {I,C}
			"Pronoun"         , // {I,C}
			"Definite1"       , // {Y,N}
			"ClosestComp"     , // {C,I}
			"IsDrug"           , // {Y,N}
			"IsDisorder"       , // {Y,N}
			"IsFinding"        , // {Y,N}
			"IsProcedure"      , // {Y,N}
			"IsAnatomicalSite" , // {Y,N}   40
			"NPHead"          , // {yes, no}
//			"Anaph"           , // numeric
//			"PermStrDist"	  , //             
			"PathLength"	  , // number of nodes in full path 37
			"NPunderVP1"	  , // NP object?
			"NPunderVP2"	  , //
			"NPunderS1"		  , // NP subject?
			"NPunderS2"       , //             
			"NPunderPP1"	  , // PP object?  
			"NPunderPP2"      , //             
			"NPSubj1"		  , //			   
			"NPSubj2"		  , //             50 
			"NPSubjBoth"	  , //			   
//			"NegatedBoth"	  , //
//			"NonNegatedBoth"  ,
			//"NPSubjBoth"    , //             
//			"Cat:Ngrams"      , //			   :n-1
//			"TK"			  ,
//			"WordsStr"			// not used, why?		 
	};

	final static String[] pron_coref_feats = ne_coref_feats;
	final static String[] dem_coref_feats = ne_coref_feats;

	public static String[] getAnaphoricityFeatures () {
		return anaph_feats;
	}

	public static String[] getNECorefFeatures () {
		return ne_coref_feats;
	}

	public static String[] getPronCorefFeatures () {
		return pron_coref_feats;
	}

	public static String[] getDemCorefFeatures() {
		return dem_coref_feats;
	}
}
