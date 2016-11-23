package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.content.MetadataField;

public class Condition {

	private MetadataField metadataField;
	private String metadataValue;
	private String operation;
	private String regex;
	
	public Condition(MetadataField metadataField, String metadataValue, String operation, String regex) {
		super();
		this.metadataField = metadataField;
		this.metadataValue = metadataValue.trim();
		this.operation = operation.trim();
		this.regex = regex.trim();
	}
	public MetadataField getMetadataField() {
		return metadataField;
	}
	public void setMetadataField(MetadataField metadataField){
		this.metadataField = metadataField;
	}
	public String getMetadataValue() {
		return metadataValue;
	}
	public void setMetadataValue(String metadataValue) {
		this.metadataValue = metadataValue;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	
	
	
}
