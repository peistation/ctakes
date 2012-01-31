package edu.mayo.bmi.uima.termspotter.cc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.mayo.bmi.uima.core.resource.FileResource;
import edu.mayo.bmi.uima.core.type.Pairs;
import edu.mayo.bmi.uima.core.type.Pair;
import edu.mayo.bmi.uima.core.cc.NonTerminalConsumer;
import edu.mayo.bmi.uima.core.type.DocumentID;
import edu.mayo.bmi.uima.pad.type.PADHit;
import edu.mayo.bmi.uima.pad.type.PADLocation;
import edu.mayo.bmi.uima.pad.type.PADTerm;
import edu.mayo.bmi.uima.pad.type.SubSection;

/**
 * @author Mayo Clinic 
 * 
 */
public class PADOffSetsRecord extends CasConsumer_ImplBase implements
		NonTerminalConsumer {
	public static final String ultrasound = "US_EXAM";
	public static final String lower_extremity = "LOWER_EXT";
	public static final String ultrasound_lower_extremity = "US_LOWER_EXT";
	public static final String ultrasound_lower_extremity_one_side_only = "US_LOWER_SOLO";
	public static final String CAT_Scan = "CT_EXAM";
	public static final String CAT_Scan_one_side_only = "CT_EXAM_SOLO";

	public static Logger iv_logger = Logger.getLogger(PADOffSetsRecord.class);
	public static final String PARAM_ALTERNATE_ALGOR = "usingAlternateAlgorithm";
	public static final String PARAM_OUTPUTFILE = "outputFileName";
	public static final String COLLECTION_SEPARATOR = "|";
	public static final String FIELD_SEPARATOR = ":";
	public static final String OS_NAME = "os.name";
	public static final String OS_WINDOWS = "Windows";
	public static final String COUNT_PLACE_HOLDER = "@@COUNT@@";
	public Boolean alternateAlgorithm = false;

	// --- default constructor ----
	public PADOffSetsRecord() {
		super();
	}

	public void initialize() throws ResourceInitializationException {
		try {
			super.initialize();
			casConsumerOffSetData = new StringBuffer();
			casConsumerOutData = new StringBuffer();

			// if(isWindows())
			// {
			String outputFileName = (String) getConfigParameterValue(PARAM_OUTPUTFILE);
			alternateAlgorithm = (Boolean) getConfigParameterValue(PARAM_ALTERNATE_ALGOR);
			iv_outputFile = new File(outputFileName);
			fos = new FileOutputStream(iv_outputFile);
			// }
		} catch (FileNotFoundException fnfe) {
			throw new ResourceInitializationException(fnfe);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processCas(CAS cas) throws ResourceProcessException {
		try {
			casConsumerOffSetData
					.replace(0, casConsumerOffSetData.length(), "");
			casConsumerOutData.replace(0, casConsumerOutData.length(), "");
			// get a list of PADTerm annotations
			processHits(cas);
		} catch (CASException ce) {
			throw new ResourceProcessException(ce);
		}
	}

	/**
	 * Used by any end-user program to get the processed data
	 * 
	 * @return
	 */
	public String getOutputXml() {
		return casConsumerOffSetData.toString();
	}

	// -- private data members ---------------

	private boolean isWindows() {
		return System.getProperty(OS_NAME).startsWith(OS_WINDOWS);
	}

	private void addPatientMetaData(JCas jcas) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator<?> annotItr = indexes.getAnnotationIndex(Pairs.type)
				.iterator();
		if (annotItr.hasNext()) {
			Pairs props = (Pairs) annotItr.next();
			FSArray fsArr = props.getPropArr();
			for (int i = 0; i < fsArr.size(); i++) {
				Pair prop = (Pair) fsArr.get(i);
				if (prop == null)
					continue;

				String key = prop.getKey();
				if (key != null && key.equalsIgnoreCase("CLINICAL_NUMBER"))
					casConsumerOffSetData.append(FIELD_SEPARATOR
							+ prop.getValue());

				if (key != null && key.equalsIgnoreCase("NOTE_DATE"))
					;
				casConsumerOffSetData.append(FIELD_SEPARATOR + prop.getValue());

			}
		}
	}

	// -- private data members ---------------

	private void processHits(CAS cas) throws CASException,
			ResourceProcessException {
		int count = 0;

		try {
			JCas jcas = cas.getJCas();
			JFSIndexRepository indexes;
			Iterator<?> annotItr;
			Iterator<?> checkExamItr;

			boolean haveUSSoloExamType = false;
			boolean haveUSExamType = false;
			boolean haveLowerExtExamType = false;
			boolean haveCombinedExamType = false;
			boolean haveLowerSoloExamType = false;
			boolean haveVRADExamType = false;

			boolean locTermsOnly = false;
			boolean noStenosis = true;
			boolean noVein = true;
			boolean noStent = true;
			boolean CTTypeExam = false;
			indexes = jcas.getJFSIndexRepository();
			boolean negatedCase = true;
			int locOnlyCount = 0;
			int termOnlyCount = 0;
			int probableCount = 0;
			int veinCount = 0;
			// Not needed for production use - add documentId that was collected
			// in the CAS initializer
			// if (isWindows()) {
			annotItr = indexes.getAnnotationIndex(DocumentID.type).iterator();
			if (annotItr.hasNext()) {
				DocumentID did = (DocumentID) annotItr.next();
				casConsumerOffSetData.append(did.getDocumentID());

			}
			// }

			// add clinic number, etc for local run
			// if (isWindows())
			addPatientMetaData(jcas);

			annotItr = indexes.getAnnotationIndex(PADHit.type).iterator();
			casConsumerOffSetData.append(COLLECTION_SEPARATOR
					+ COUNT_PLACE_HOLDER + COLLECTION_SEPARATOR);

			// Need subsection for special case where Ultrasound exams are being
			// implemented:
			// Case : UNK: if no relevant documents OR negation of positive
			// evidence in US or V+IRA

			checkExamItr = indexes.getAnnotationIndex(SubSection.type)
					.iterator();
			while (checkExamItr.hasNext()) {
				SubSection ssId = (SubSection) checkExamItr.next();
				// US_LOWER_SOLO (ultrasound_lower_extremity_one_side_only),
				// CT_EXAM_SOLO (CAT_Scan_one_side_only)
				if (ssId.getParentSectionId().compareTo(
						ultrasound_lower_extremity_one_side_only) == 0
						|| ssId.getParentSectionId().compareTo(
								"CAT_Scan_one_side_only") == 0)
					haveLowerSoloExamType = true;
				if (ssId.getParentSectionId().indexOf("V_IRAD") >= 0
						|| ssId.getParentSectionId().compareTo("V&IRAD") == 0)
					haveVRADExamType = true;
				// US_EXAM (ultrasound),
				if (ssId.getParentSectionId().compareTo(ultrasound) == 0)
					haveUSExamType = true;
				// LOWER_EXT (lower_extremity),

				if (ssId.getParentSectionId().compareTo(lower_extremity) == 0)
					haveLowerExtExamType = true;
				// US_LOWER_EXT (ultrasound_lower_extremity),

				if (ssId.getParentSectionId().compareTo(
						ultrasound_lower_extremity) == 0)
					haveCombinedExamType = true;
				// US_LOWER_SOLO (ultrasound_lower_extremity_one_side_only),

				if (ssId.getParentSectionId().compareTo(
						ultrasound_lower_extremity_one_side_only) == 0)
					haveUSSoloExamType = true;
				// CT_EXAM (CAT_Scan),

				if (ssId.getParentSectionId().indexOf(CAT_Scan) > 0
						|| ssId.getParentSectionId().compareTo(CAT_Scan) == 0)
					CTTypeExam = true;
			}
			boolean negCount = false;
			boolean probableCase = false;
			boolean globalProbable = false;
			int balanceCount = 0;
			while (annotItr.hasNext()) {
				count++;
				negatedCase = false;
				// probableCase = false;
				if (count >= 1) {
					casConsumerOffSetData.append("(");
					casConsumerOutData.append(COLLECTION_SEPARATOR);
				}

				// add segment from term or location
				PADHit uaHit = (PADHit) annotItr.next();
				if (uaHit.getUaTerm() != null)
					casConsumerOutData.append(((PADTerm) uaHit.getUaTerm())
							.getSegmentID());
				else if (uaHit.getUaLocation() != null)
					casConsumerOutData.append(((PADLocation) uaHit
							.getUaLocation()).getSegmentID());

				// add location and term data
				PADLocation ual = uaHit.getUaLocation();
				PADTerm uat = uaHit.getUaTerm();

				if (ual != null) {
					casConsumerOutData.append(COLLECTION_SEPARATOR
							+ ual.getCoveredText());
					casConsumerOffSetData.append(ual.getBegin() + "-"
							+ ual.getEnd() + ":");
					if (ual.getCertainty() == -1
							&& uat == null
							|| (ual.getCertainty() == -1 && uat != null
									&& uat.getTypeID() != disorderStenosis && ual
									.getCertainty() == -1)) {
						negatedCase = true;
					} else if (ual.getIsStandAlone() != 1
							&& ual.getCertainty() != -1
							&& ual.getStatus() == disorderPatent) {
						probableCase = true;
						probableCount++;
					} else
						probableCase = false;
				} else {
					casConsumerOutData.append(COLLECTION_SEPARATOR + " ");
					casConsumerOffSetData.append("-1:");
					if (uat.getTypeID() == disorderStenosis
							&& uat.getCertainty() != -1)
						noStenosis = false;
					if (uat != null && uat.getCertainty() == -1
							&& uat.getTypeID() != disorderStenosis
							&& uat.getTypeID() != anatomicalSiteExclusion)
						negatedCase = true;
					else if (uat != null
							&& ((uat.getIsStandAlone() != 1 || uat.getTypeID() == disorderPatent) && uat
									.getCertainty() != -1)) {
						probableCase = true;
						probableCount++;
					} else
						probableCase = false;
				}

				if (uat != null) {
					casConsumerOutData.append(COLLECTION_SEPARATOR
							+ uat.getCoveredText());
					casConsumerOffSetData.append(uat.getBegin() + "-"
							+ uat.getEnd() + ")");
					if (uat.getTypeID() == disorderStenosis
							&& uat.getCertainty() != -1)
						noStenosis = false;
					if (uat.getCertainty() == -1
							&& uat.getTypeID() != disorderStenosis
							&& uat.getStatus() != disorderPatent
							&& uat.getTypeID() != anatomicalSiteExclusion
							&& uat.getIsStandAlone() != 1)

						negatedCase = true;
					else if (uat.getStatus() == disorderPatent
							&& uat.getIsStandAlone() != 1) {
						probableCase = true;
						probableCount++;
					} else if (!negatedCase)
						probableCase = false;
				} else {
					casConsumerOutData.append(COLLECTION_SEPARATOR + " ");
					casConsumerOffSetData.append("-1)");
					if (ual != null && ual.getCertainty() == -1
							&& ual.getStatus() != disorderPatent)
						negatedCase = true;
					else if (ual.getIsStandAlone() != 1
							&& ual.getCertainty() != -1
							&& ual.getStatus() == disorderPatent) {
						probableCase = true;
						probableCount++;
					}

					else
						probableCase = false;
				}

				if (probableCase)
					globalProbable = true;
				if ((negatedCase || probableCase)) {
					count--;
					negCount = true;
					if (uat != null && !probableCase
							&& uat.getTypeID() != disorderStenosis
							&& uat.getTypeID() != disorderPatent) {
						if (ual == null || (ual != null)
								&& ual.getTypeID() != disorderPatent
								&& ual.getCertainty() == -1)
							balanceCount--;

					}
				} else
					balanceCount++;

			}
			annotItr = indexes.getAnnotationIndex(PADLocation.type).iterator();
			while (annotItr.hasNext()) {
				PADLocation location = (PADLocation) annotItr.next();
				Iterator<?> annotHitItr = indexes.getAnnotationIndex(
						PADHit.type).iterator();
				boolean skip = false;
				if (location.getCoveredText().indexOf("vein") != -1) {
					noVein = false;
					veinCount++;
				}
				while (annotHitItr.hasNext() && !skip) {
					PADHit uaHit = ((PADHit) annotHitItr.next());
					if (uaHit.getUaLocation() != null
							&& location.getBegin() == uaHit.getUaLocation()
									.getBegin())
						skip = true;

				}
				if (!skip) {
					if (location.getCertainty() == -1) {
						locTermsOnly = true;
					}

					if (location.getCertainty() != -1) {

						locOnlyCount++;
						casConsumerOffSetData.append("(");

						casConsumerOffSetData.append("-1:");
						casConsumerOffSetData.append(location.getBegin() + "-"
								+ location.getEnd() + ")");
						casConsumerOutData.append(COLLECTION_SEPARATOR);
						casConsumerOutData.append(location.getSegmentID());
						casConsumerOutData.append(COLLECTION_SEPARATOR
								+ "**NO TERM**");
						casConsumerOutData.append(COLLECTION_SEPARATOR
								+ location.getCoveredText());

					} else if (location.getCertainty() != -1
							&& location.getTypeID() != disorderStenosis
							&& location.getTypeID() != anatomicalSiteExclusion
							&& location.getStatus() == disorderPatent)
						locOnlyCount--;
				}
			}
			annotItr = indexes.getAnnotationIndex(PADTerm.type).iterator();
			while (annotItr.hasNext()) {
				PADTerm term = (PADTerm) annotItr.next();
				Iterator<?> annotHitItr = indexes.getAnnotationIndex(
						PADHit.type).iterator();
				boolean skip = false;
				while (annotHitItr.hasNext() && !skip) {
					PADHit uaHit = ((PADHit) annotHitItr.next());
					if (uaHit.getUaTerm() != null
							&& term.getBegin() == uaHit.getUaTerm().getBegin())
						skip = true;
				}
				if (!skip) {
					locTermsOnly = false;
					if (term.getTypeID() == disorderStenosis
							&& term.getCertainty() != -1)
						noStenosis = false;
					if (term.getStatus() == disorderPatent
							&& (term.getCertainty() != -1 || term.getTypeID() == disorderStenosis
									&& term.getTypeID() != anatomicalSiteExclusion)) {
						if (term.getStatus() == disorderPatent && count == 0) {
							globalProbable = true;
							if (term.getCoveredText().compareToIgnoreCase(
									"stent") == 0
									|| term.getCoveredText()
											.compareToIgnoreCase("stents") == 0
									|| term.getCoveredText()
											.compareToIgnoreCase("stented") == 0)
								noStent = false;
						}

						termOnlyCount++;
						casConsumerOffSetData.append("(");
						casConsumerOffSetData.append(term.getBegin() + "-"
								+ term.getEnd() + ":-1)");
						casConsumerOutData.append(COLLECTION_SEPARATOR);
						casConsumerOutData.append(term.getSegmentID());
						casConsumerOutData.append(COLLECTION_SEPARATOR
								+ term.getCoveredText());
						casConsumerOutData.append(COLLECTION_SEPARATOR
								+ "**NO LOC**");

					} else if (term.getCertainty() != -1
							&& term.getTypeID() != disorderStenosis
							&& term.getTypeID() != anatomicalSiteExclusion
							&& term.getTypeID() == disorderPatent)
						termOnlyCount--;
				}
			}
			int loc = casConsumerOffSetData.indexOf(COUNT_PLACE_HOLDER);
			casConsumerOffSetData.append(casConsumerOutData.toString());

			boolean noRelatedExam = !haveUSSoloExamType && !haveUSExamType
					&& !haveLowerExtExamType && !haveCombinedExamType
					&& !haveLowerSoloExamType && !haveVRADExamType;
			String classification = null;
			if (!alternateAlgorithm) {
				// If there is no related exam type and no positive or negative
				// evidence, classify as UNKNOWN;
				if ((!haveUSSoloExamType && !haveUSExamType
						&& !haveLowerExtExamType && !haveCombinedExamType
						&& !haveLowerSoloExamType && !CTTypeExam && !negCount
						&& !negatedCase && !globalProbable && count < 1)
						|| noRelatedExam && !CTTypeExam)
					classification = String.valueOf(-1);
				// If there is positive evidence in an applicable exam, classify
				// as POS;
				else if (((balanceCount > 0 || count > 0) && (haveVRADExamType
						|| haveUSSoloExamType || haveUSExamType
						|| haveLowerExtExamType || haveCombinedExamType
						|| haveLowerSoloExamType || CTTypeExam)))
					classification = String.valueOf(count);
				// If there is explicit evidence of negation of positive
				// evidence or lower solo extremity exam with evidence of no
				// stenosis and no discussion concerning veins and other
				// locations of concern are mentioned nor probable PAD
				// discussion, classify as NEG ;
				else if ((negatedCase || negCount)
						&& ((balanceCount < 0 && locOnlyCount + balanceCount < 1) || (noStenosis
								&& noStent && noVein && !globalProbable)
								&& haveCombinedExamType
								&& (!locTermsOnly || negatedCase))
						|| count < 0
						|| (haveLowerSoloExamType && haveUSExamType && locOnlyCount > 0))
					classification = String.valueOf(0);
				// If there are no POS or NEG evidence OR
				// o if there is negation of positive evidence in an Ultrosound
				// or V+IRAD report and no probable and negated counts or vein
				// counts,
				// o classify as UNKNOWN;
				else if ((haveUSSoloExamType || haveUSExamType
						|| haveCombinedExamType || haveLowerExtExamType
						|| haveLowerSoloExamType || haveVRADExamType || CTTypeExam)
						&& ((!probableCase && probableCount < 1 && (negatedCase || negCount)) || veinCount > 0))
					classification = String.valueOf(-1);
				// Otherwise, classify as PROB;
				else
					classification = "prob";
			} else {
				classification = calculateRecordLevelClassification(count,
						termOnlyCount,

						locOnlyCount, balanceCount, probableCount, veinCount,
						noStent, locTermsOnly, noVein, noStenosis, CTTypeExam,
						haveLowerExtExamType, haveCombinedExamType,
						haveUSSoloExamType, haveLowerSoloExamType,
						haveUSExamType, negCount, negatedCase, globalProbable);
			}

			casConsumerOffSetData.replace(loc, loc
					+ COUNT_PLACE_HOLDER.length(), classification);

			// if (isWindows()) {
			casConsumerOffSetData.append("\n");
			fos.write(casConsumerOffSetData.toString().getBytes());
			// }

		} catch (IOException ioe) {
			throw new ResourceProcessException(ioe);
		}
	}

	// -- private data members ---------------

	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
		// TODO Auto-generated method stub
		super.collectionProcessComplete(arg0);
		File file = new File(new File(iv_outputFile.getAbsolutePath())
				.getParent());

		holdResults = new File(file + System.getProperty("file.separator")
				+ "Summary_PADRadiology.csv");
		if (file.isDirectory()) {
			String[] processFiles = file.list();

			if (!holdResults.exists())
				store(holdResults.getAbsolutePath(), drugHeaders);
			for (int i = 0; i < processFiles.length; i++) {
				System.out.println("Process Each File in '" + file
						+ System.getProperty("file.separator")
						+ processFiles[i] + "'");
				File nextFile = new File(file
						+ System.getProperty("file.separator")
						+ processFiles[i]);
				boolean hasNext = true;

				if (!nextFile.isDirectory()) {
					int numPADConfirmed = 0;
					int numPADConfirmedWeight = 0;
					int numPADNegative = 0;
					int numPADProbable = 0;
					int countPatientRecords = 0;
					String fileName = nextFile.getAbsolutePath();
					List<String[][]> findPatientPADClassification = loadFieldContents(fileName);
					List<String[][]> nextPatientPADClassification = loadFieldContents(fileName);
					Iterator<String[][]> searchNextPatient = nextPatientPADClassification
							.iterator();
					searchNextPatient.next();
					Iterator<String[][]> searchPatientClassification = findPatientPADClassification
							.iterator();
					String[][] nextPatientClassInstance = null;
					String nextPatientID = "";
					while (searchPatientClassification.hasNext()) {
						countPatientRecords++;
						if (hasNext = searchNextPatient.hasNext()) {

							nextPatientClassInstance = searchNextPatient.next();
							nextPatientID = nextPatientClassInstance[0][0];
						}
						String[][] patientClassInstance = searchPatientClassification
								.next();

						String patientID = patientClassInstance[0][0];
						String classificationPAD = patientClassInstance[1][0];

						if (classificationPAD.indexOf("-1") == -1) {
							if (classificationPAD.indexOf("prob") == 0) {
								numPADProbable++;
							} else if (classificationPAD.indexOf("0") == 0) {
								numPADNegative++;
							} else {
								numPADConfirmed++;
								numPADConfirmedWeight = numPADConfirmedWeight
										+ new Integer(classificationPAD)
												.intValue();
							}
						}
						// set precedence order as PAD-Yes > PAD-prob >
						// PAD-no/unknown
						if (nextPatientID.compareTo(patientID) != 0 || !hasNext
								&& nextPatientID.compareTo(patientID) == 0) {
							if ((numPADNegative > numPADProbable)
									&& (numPADNegative > numPADConfirmed && numPADNegative > numPADConfirmedWeight)) {
								store(holdResults.getAbsolutePath(), patientID
										+ ",no");
							} else if ((numPADProbable > numPADNegative)
									&& (numPADProbable > numPADConfirmed)
									|| (numPADConfirmed > 0
											&& numPADNegative == numPADConfirmed && numPADConfirmedWeight == numPADNegative)) {
								store(holdResults.getAbsolutePath(), patientID
										+ ",probable");
							} else if ((numPADConfirmed > numPADNegative || numPADConfirmedWeight > numPADNegative)
									&& (numPADConfirmed > numPADProbable || numPADConfirmedWeight > numPADProbable)) {
								store(holdResults.getAbsolutePath(), patientID
										+ ",yes");
							} else if ((numPADConfirmed > numPADNegative || numPADConfirmedWeight > numPADNegative)
									&& (numPADConfirmed == numPADProbable || numPADConfirmedWeight > numPADProbable)) {
								if (numPADConfirmedWeight / numPADProbable > 1)
									store(holdResults.getAbsolutePath(),
											patientID + ",yes");
								else
									store(holdResults.getAbsolutePath(),
											patientID + ",probable");
							} else if ((numPADConfirmed > numPADProbable || numPADConfirmedWeight > numPADProbable)
									&& (numPADConfirmed == numPADNegative || numPADConfirmedWeight > numPADNegative)) {
								if (numPADConfirmedWeight / numPADNegative > 1)
									store(holdResults.getAbsolutePath(),
											patientID + ",yes");
								else
									store(holdResults.getAbsolutePath(),
											patientID + ",unknown");
							} else if ((numPADNegative > numPADConfirmed)
									&& (numPADNegative == numPADProbable)) {
								store(holdResults.getAbsolutePath(), patientID
										+ ",probable");
							} else if ((numPADNegative > numPADProbable)
									&& (numPADNegative == numPADConfirmed)) {
								if (numPADConfirmed > 0
										&& numPADConfirmedWeight
												/ numPADNegative > 1)
									store(holdResults.getAbsolutePath(),
											patientID + ",yes");
								else
									store(holdResults.getAbsolutePath(),
											patientID + ",unknown");
							} else if ((numPADProbable > numPADConfirmed)
									&& (numPADProbable == numPADNegative)) {
								store(holdResults.getAbsolutePath(), patientID
										+ ",unknown");
							} else if ((numPADProbable > numPADNegative)
									&& (numPADProbable == numPADConfirmed)) {
								if (numPADConfirmed > 0
										&& numPADConfirmedWeight
												/ numPADProbable > 1)
									store(holdResults.getAbsolutePath(),
											patientID + ",yes");
								else
									store(holdResults.getAbsolutePath(),
											patientID + ",probable");
							} else {
								store(holdResults.getAbsolutePath(), patientID
										+ ",unknown");
							}
							numPADConfirmed = 0;
							numPADConfirmedWeight = 0;
							numPADNegative = 0;
							numPADProbable = 0;
							countPatientRecords = 0;
						}
					}
				}
			}
		}

		// if (isWindows())
		fos.close();
	}

	/**
	 * Private method 'calculateRecordLevelClassification' is responsible for
	 * providing the PAD classification given the factors; #1 Type of
	 * examination ("CT_EXAM", "LOWER_EXT", "US_LOWER_EXT", "US_EXAM_SOLO",
	 * "US_LOWER_SOLO or CT_EXAM_SOLO, and "US_EXAM") #2 Number of hits, and, if
	 * applicable, the difference if both exist ("numberOfHits" and
	 * "differentialHitCount") #3 Number of term only mentions, site only
	 * mentions, probable evidence, mentions of vein related terms, and mention
	 * of stent related terms ("numberOfTermOnly", "numberOfLocationOnly",
	 * "numberOfProbable", "numberVeinMentions", and "noMentionOfStents") #4
	 * Whether there is a mention of veins or stenosis related terms
	 * ("noMentionOfVeins" and "noMentionOfStenosis") #5 Whether there is
	 * evidence of probable, location terms only, negation and possible negation
	 * ("foundEvidenceOfProbable", "locationTermsOnly", "haveNegativeCases",
	 * "possibleNegativeCases"); #6 "noPadMentionThreshold" is a somewhat
	 * arbitrary value used to measure a relative weight of evidence against
	 * other counts.
	 * 
	 * 
	 * @param numberOfHits
	 * @param numberOfTermOnly
	 * @param numberOfLocationOnly
	 * @param differentialHitCount
	 * @param numberOfProbable
	 * @param numberVeinMentions
	 * @param noMentionOfStents
	 * @param locationTermsOnly
	 * @param noMentionOfVeins
	 * @param noMentionOfStenosis
	 * @param haveCT_EXAM
	 * @param haveLOWER_EXT
	 * @param haveUS_LOWER_EXT
	 * @param haveUS_EXAM_SOLO
	 * @param haveUS_LOWER_SOLOorCT_EXAM_SOLO
	 * @param haveUS_EXAM
	 * @param haveNegativeCases
	 * @param possibleNegativeCases
	 * @param foundEvidenceOfProbable
	 * @return
	 */
	private String calculateRecordLevelClassification(int numberOfHits,
			int numberOfTermOnly, int numberOfLocationOnly,
			int differentialHitCount, int numberOfProbable,
			int numberVeinMentions, boolean noMentionOfStents,
			boolean locationTermsOnly, boolean noMentionOfVeins,
			boolean noMentionOfStenosis, boolean haveCT_EXAM,
			boolean haveLOWER_EXT, boolean haveUS_LOWER_EXT,
			boolean haveUS_EXAM_SOLO, boolean haveUS_LOWER_SOLOorCT_EXAM_SOLO,
			boolean haveUS_EXAM, boolean haveNegativeCases,
			boolean possibleNegativeCases, boolean foundEvidenceOfProbable) {

		if (numberOfHits == 0 && numberOfTermOnly + numberOfLocationOnly >= 0)
			differentialHitCount = 0;

		if ((differentialHitCount < 0 || !haveCT_EXAM)
				&& numberOfHits == 0
				&& !haveNegativeCases
				&& !foundEvidenceOfProbable
				&& !haveLOWER_EXT
				&& !haveUS_LOWER_EXT
				|| (!haveUS_EXAM && !haveUS_LOWER_EXT && !haveCT_EXAM
						&& !haveUS_LOWER_SOLOorCT_EXAM_SOLO && !haveLOWER_EXT
						&& numberOfHits == 0 && !haveNegativeCases && (locationTermsOnly && numberOfLocationOnly > noPadMentionThreshold))
				|| ((haveUS_EXAM || haveUS_LOWER_EXT) && numberOfHits == 0
						&& !noMentionOfStenosis && !foundEvidenceOfProbable && (!locationTermsOnly
						|| !noMentionOfVeins || haveNegativeCases))
				|| ((numberOfLocationOnly <= noPadMentionThreshold)
						&& numberOfHits == 0
						&& (!haveNegativeCases || !noMentionOfVeins)
						&& !noMentionOfStenosis && !foundEvidenceOfProbable && (numberOfTermOnly <= 0 || differentialHitCount < 0))
				|| noMentionOfStenosis && !haveUS_LOWER_EXT && !haveLOWER_EXT
				&& numberOfHits == 0 && !foundEvidenceOfProbable)
			return String.valueOf(-1);
		else if (numberOfHits > 0
				&& ((!noMentionOfStenosis
						&& (haveCT_EXAM || haveLOWER_EXT || haveUS_LOWER_EXT) && (!foundEvidenceOfProbable
						|| !possibleNegativeCases || numberOfHits >= noPadMentionThreshold))
						|| (numberOfHits > 0 && differentialHitCount >= 0
								&& !foundEvidenceOfProbable && (!haveUS_LOWER_SOLOorCT_EXAM_SOLO && !haveUS_EXAM_SOLO)) || (numberOfHits
						+ numberOfLocationOnly + numberOfTermOnly >= noPadMentionThreshold)))
			return String.valueOf(numberOfHits);
		else if (((haveNegativeCases || (numberOfLocationOnly >= noPadMentionThreshold
				&& numberOfTermOnly <= 0 && (haveLOWER_EXT || haveUS_EXAM || haveUS_LOWER_EXT)))
				&& !foundEvidenceOfProbable && !haveUS_LOWER_SOLOorCT_EXAM_SOLO)
				|| noMentionOfStenosis
				&& (numberOfHits != 0 && numberOfTermOnly != 0
						&& differentialHitCount != 0 || haveLOWER_EXT)
				&& !foundEvidenceOfProbable
				&& noMentionOfVeins
				&& (haveCT_EXAM || haveLOWER_EXT))
			return String.valueOf(0);
		else if (foundEvidenceOfProbable
				&& (haveLOWER_EXT || haveUS_EXAM
						|| haveUS_LOWER_SOLOorCT_EXAM_SOLO || haveUS_EXAM_SOLO
						|| haveUS_LOWER_EXT || ((haveCT_EXAM || numberOfProbable >= noPadMentionThreshold)
						&& noMentionOfStents
						&& (noMentionOfVeins || (!noMentionOfVeins && numberVeinMentions < numberOfProbable
								+ numberOfLocationOnly)) && !haveUS_EXAM_SOLO && !haveUS_LOWER_SOLOorCT_EXAM_SOLO)))
			return "prob";

		else
			return String.valueOf(-1);
	}

	/**
	 * Loads text from a file. Specialized to load array idAndDate and return it
	 * too
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void store(String filename, String lineToStore)
			throws FileNotFoundException, IOException {
		int howMany = 132;
		boolean skipDate = false;

		File f = new File(filename);
		if (!f.exists())
			f.createNewFile();

		ByteArrayOutputStream bout = new ByteArrayOutputStream(howMany * 4);

		DataOutputStream dout = new DataOutputStream(bout);
		FileOutputStream fos = new FileOutputStream(filename, true);

		if (!skipDate)
			dout.writeBytes(lineToStore + '\n');

		try {
			if (!skipDate) {
				bout.writeTo(fos);
				fos.flush();
			}
		} finally {
			fos.close();
			bout.close();
			dout.close();
		}

	}

	/**
	 * Loads text from a file. Specialized to load array idAndDate and return it
	 * too
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static List<String[][]> loadFieldContents(String filename)
			throws FileNotFoundException, IOException {

		String[][] padClass = null;
		List<String[][]> listIdandClassification = new ArrayList<String[][]>();
		File f = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(f));
		// br.readLine();// dummy line to go beyond columm headers

		String line = br.readLine();

		int index = 0;

		while ((line != null) && (line != "") && (line.length() > 0)) {
			String patientID = line.substring(0, line.indexOf('|'));
			String temp = line.substring(line.indexOf('|') + 1);
			String padClassification = temp.substring(0, temp.indexOf('|'));
			padClass = new String[][] { { patientID }, { padClassification } };
			listIdandClassification.add(index, padClass);
			index++;
			line = br.readLine();
		}
		br.close();

		return listIdandClassification;
	}

	// -- private data members ---------------
	private File iv_outputFile;
	private StringBuffer casConsumerOffSetData;
	private StringBuffer casConsumerOutData;
	private FileOutputStream fos;
	private File holdResults;
	private String drugHeaders = "clinic,case_type";
	private int anatomicalSiteExclusion = 9;
	private int disorderPatent = 7;
	private int disorderStenosis = 8;
	private static int noPadMentionThreshold = 2;
}
