package org.dspace.app.xmlui.aspect.ELProcessor;

public class Condition {

	private String metadataField;
	private String metadataValue;
	private String operation;
	
	public Condition(String metadataField, String metadataValue, String operation) {
		super();
		this.metadataField = metadataField.trim();
		this.metadataValue = metadataValue.trim();
		this.operation = operation.trim();
	}
	public String getMetadataField() {
		return metadataField;
	}
	public void setMetadataField(String metadataField) {
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
	
	
	
}
