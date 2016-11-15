package org.dspace.app.xmlui.aspect.ELProcessor;

public class FactoryManager {
	
	private HandleManager handleManager;
	private ConditionManager conditionManager;
	private MetadataManager metadataManager;

	public HandleManager getHandleManager(){
		if(handleManager == null){
			handleManager = new HandleManager();
		}
		return handleManager;
	}
	
	public ConditionManager getConditionManager(){
		if(conditionManager == null){
			conditionManager = new ConditionManager();
		}
		return conditionManager;
	}

	public MetadataManager getMetadataManager(){
		if(metadataManager == null){
			metadataManager = new MetadataManager();
		}
		return metadataManager;
	}
	
}
