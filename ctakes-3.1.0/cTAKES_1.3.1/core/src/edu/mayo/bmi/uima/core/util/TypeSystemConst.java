/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.core.util;

/**
 * Contains a set of constants for feature values.
 * 
 * @author Mayo
 */
public interface TypeSystemConst
{
	public static final int NE_TYPE_ID_UNKNOWN = 0;
	public static final int NE_TYPE_ID_DRUG = 1;
	public static final int NE_TYPE_ID_DISORDER = 2;
	public static final int NE_TYPE_ID_FINDING = 3;
//	public static final int NE_TYPE_ID_UNUSED  = 4;
	public static final int NE_TYPE_ID_PROCEDURE = 5;
	public static final int NE_TYPE_ID_ANATOMICAL_SITE = 6;

	public static final int NE_DISCOVERY_TECH_DICT_LOOKUP = 1;

//	public static final int NE_DIAG_STATUS_CONFIRMED = 0;
//	public static final int NE_DIAG_STATUS_HISTORY_OF = 1;
//	public static final int NE_DIAG_STATUS_FAM_HISTORY_OF = 2;
//	public static final int NE_DIAG_STATUS_PROBABLE = 3;

//	public static final int NE_CERTAINTY_POSITIVE = 1;
//	public static final int NE_CERTAINTY_NEGATED = -1;
}
