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
package edu.mayo.bmi.uima.ner.elements;

public class ConfidenceScoreElement {
	
	private double confidence = 0;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public ConfidenceScoreElement(){
    	
    
		
	}
    
	public ConfidenceScoreElement(double score, int beginOffset, int endOffset){
		confidence = score;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public double getConfidenceScoreElement(){
		return confidence;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
