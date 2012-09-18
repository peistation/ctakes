package edu.mayo.bmi.uima.core.util;

/**
 * Class containing relevant information for 
 * a given document section
 * @author andreea bodnari
 *
 */
public class DocumentSection {
	private int startLine;
	private int endLine;
	
	private String content;
	private String sectionName;
	
	public DocumentSection(int start, int end, String text){
		this.startLine = start;
		this.endLine = end;
		this.content = text;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

}
