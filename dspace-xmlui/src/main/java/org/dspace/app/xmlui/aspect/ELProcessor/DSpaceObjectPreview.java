package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.content.MetadataField;

public class DSpaceObjectPreview {
	
	private String handle;
	private String metadataName;
	private String oldValue;
	private String newValue;
	
	public DSpaceObjectPreview(String handle, MetadataField metadataField, String oldValue, String newValue) {
		super();
		this.handle = handle;
		this.metadataName = metadataField.getMetadataSchema().getName()+metadataField.getElement();
		if(metadataField.getQualifier() != null){
			this.metadataName += metadataField.getQualifier();
		}
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getMetadataName() {
		return metadataName;
	}
	public void setMetadataName(String metadataName) {
		this.metadataName = metadataName;
	}
	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
	

}
