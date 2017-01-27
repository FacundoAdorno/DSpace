package org.dspace.app.xmlui.aspect.ELProcessor;

public class ResolverFactory {
	
	private HandleResolver handleResolver;
	private ConditionForValidateResolver conditionSelectResolver;
	private ConditionForAddModifyResolver conditionAddModifyResolver;
	private ConditionForDeleteResolver conditionDeleteResolver;
	private MetadataResolver metadataResolver;
	private UpdateResolver updateResolver;
	private SelectResolver selectResolver;

	public HandleResolver getHandleResolver(){
		if(handleResolver == null){
			handleResolver = new HandleResolver();
		}
		return handleResolver;
	}
	
	public ConditionForValidateResolver getConditionSelectResolver(){
		if(conditionSelectResolver == null){
			conditionSelectResolver = new ConditionForValidateResolver();
		}
		return conditionSelectResolver;
	}
	
	public ConditionForAddModifyResolver getConditionAddModifyResolver(){
		if(conditionAddModifyResolver == null){
			conditionAddModifyResolver = new ConditionForAddModifyResolver();
		}
		return conditionAddModifyResolver;
	}
	
	public ConditionForDeleteResolver getConditionDeleteResolver(){
		if(conditionDeleteResolver == null){
			conditionDeleteResolver = new ConditionForDeleteResolver();
		}
		return conditionDeleteResolver;
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
	
	public SelectResolver getSelectResolver(){
		if(selectResolver == null){
			selectResolver = new SelectResolver();
		}
		return selectResolver;
	}
	
}
