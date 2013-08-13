/**
 * Contains information from a single document - is not the result of data mining.
 * 
 */
package edu.mayo.bmi.uima.ner;

public interface DrugModel {

	
	public String getPrimaryAssociatedCodeElement();
	public String getSecondaryAssociatedCodeElement();
	public String getStartDateElement();
	public String getEndDateElement();
	public String getDosageElement();
	public String getFrequencyElement();
	public String getFrequencyUnitElement();
	public String getFormElement();
	public String getDurationElement();
	public String getRouteElement();
	public String getDrugChangeStatusElement();
	public String getStrengthElement();
	public double getConfidenceScoreElement();

	
	public void setPrimaryAssociatedCodeElement(String name, int beginOffset, int endOffset);
	public void setSecondaryAssociatedCodeElement(String name, int beginOffset, int endOffset);
	public void setFormElement(String name, int beginOffset, int endOffset);
	public void setStartDateElement(String name, int beginOffset, int endOffset);
	public void setEndDateElement(String name, int beginOffset, int endOffset);
	public void setDosageElement(String name, int beginOffset, int endOffset);
	public void setFrequencyElement(String name, int beginOffset, int endOffset);
	public void setFrequencyUnitElement(String name, int beginOffset, int endOffset);
	public void setDurationElement(String name, int beginOffset, int endOffset);
	public void setRouteElement(String name, int beginOffset, int endOffset);
	public void setStrengthElement(String name, int beginOffset, int endOffset);
	public void setDrugChangeStatusElement(String name, int beginOffset, int endOffset);
	public void setConfidenceScoreElement(double score, int beginOffset, int endOffset);
	

}

