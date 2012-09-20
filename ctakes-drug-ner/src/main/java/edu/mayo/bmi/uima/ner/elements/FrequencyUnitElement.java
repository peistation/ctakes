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

public class FrequencyUnitElement {
	
	private String frequencyUnit = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
	public static final String DAILY = "day";
	public static final String WEEKLY = "week";
	public static final String MONTHLY = "month";
	public static final String HOURLY = "hour";
	public static final String EVERYOTHERDAY = "every-other-day";
	public static final String ASNEEDED = "as-needed";
	public static final String OTHER = "other";
	

    public FrequencyUnitElement(){
    	
    
		
	}
    
	public FrequencyUnitElement(String freq, int beginOffset, int endOffset){
		frequencyUnit = freq;
		begOff = beginOffset;
		endOff = endOffset;

	}
	
	public String getFrequencyUnitMention(){
		return frequencyUnit;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}
	

}
