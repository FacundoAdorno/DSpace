package org.dspace.app.xmlui.aspect.ELProcessor;

public class ResolverFactory {
	
	private HandleResolver handleResolver;
	private ConditionForSelectResolver conditionSelectResolver;
	private ConditionForUpdateResolver conditionUpdateResolver;
	private MetadataResolver metadataResolver;
	private UpdateResolver updateResolver;

	public HandleResolver getHandleResolver(){
		if(handleResolver == null){
			handleResolver = new HandleResolver();
		}
		return handleResolver;
	}
	
	public ConditionForSelectResolver getConditionSelectResolver(){
		if(conditionSelectResolver == null){
			conditionSelectResolver = new ConditionForSelectResolver();
		}
		return conditionSelectResolver;
	}
	
	public ConditionForUpdateResolver getConditionUpdateResolver(){
		if(conditionUpdateResolver == null){
			conditionUpdateResolver = new ConditionForUpdateResolver();
		}
		return conditionUpdateResolver;
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
