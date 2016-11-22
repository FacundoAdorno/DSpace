package org.dspace.app.xmlui.aspect.ELProcessor;

public class ResolverFactory {
	
	private HandleResolver handleResolver;
	private ConditionResolver conditionResolver;
	private MetadataResolver metadataResolver;
	private UpdateResolver updateResolver;

	public HandleResolver getHandleResolver(){
		if(handleResolver == null){
			handleResolver = new HandleResolver();
		}
		return handleResolver;
	}
	
	public ConditionResolver getConditionResolver(){
		if(conditionResolver == null){
			conditionResolver = new ConditionResolver();
		}
		return conditionResolver;
	}

	public MetadataResolver getMetadataResolver(){
		if(metadataResolver == null){
			metadataResolver = new MetadataResolver();
		}
		return metadataResolver;
	}
	
	public UpdateResolver getUpdateResolver(){
		if(updateResolver == null){
			updateResolver = new UpdateResolver();
		}
		return updateResolver;
	}
	
}
