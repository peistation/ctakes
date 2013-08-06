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
package edu.mayo.bmi.uima.lookup.ae;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;

import edu.mayo.bmi.uima.core.resource.JdbcConnectionResource;

/**
 * Implementation that takes UMLS dictionary lookup hits and stores as NamedEntity 
 * objects only the ones that have a SNOMED synonym, by looking in a database 
 * for SNOMED codes that map to the identified CUI.
 * 
 * @author Mayo Clinic
 */
public class UmlsToSnomedDbConsumerImpl extends UmlsToSnomedConsumerImpl implements
		LookupConsumer
{
	
	private final String DB_CONN_RESRC_KEY_PRP_KEY = "dbConnExtResrcKey";
	private final String MAP_PREP_STMT_PRP_KEY = "mapPrepStmt";

	private PreparedStatement mapPrepStmt;

	public UmlsToSnomedDbConsumerImpl(AnnotatorContext aCtx, Properties properties)
			throws Exception
	{
		super(aCtx, properties);

		String resrcName = props.getProperty(DB_CONN_RESRC_KEY_PRP_KEY);
		JdbcConnectionResource resrc = (JdbcConnectionResource) aCtx.getResourceObject(resrcName);

		String prepStmtSql = props.getProperty(MAP_PREP_STMT_PRP_KEY);
		Connection conn = resrc.getConnection();
		mapPrepStmt = conn.prepareStatement(prepStmtSql);

	}


	/**
	 * Queries the given UMLS CUI against the DB. Returns a set of SNOMED codes.
	 * 
	 * @param umlsCode
	 * @return
	 * @throws SQLException
	 */
	protected Set getSnomedCodes(String umlsCode) throws SQLException
	{
		Set codeSet = new HashSet();
		mapPrepStmt.setString(1, umlsCode);
		ResultSet rs = mapPrepStmt.executeQuery();
		while (rs.next())
		{
			String snomedCode = rs.getString(1).trim();
			codeSet.add(snomedCode);
		}
		
		return codeSet;

	}

}