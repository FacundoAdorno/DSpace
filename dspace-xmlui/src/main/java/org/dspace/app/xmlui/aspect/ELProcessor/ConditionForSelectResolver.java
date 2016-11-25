package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.List;
import java.util.UUID;

public class ConditionForSelectResolver extends ConditionResolver{

	
	public void getItemsFromCondition(String condition, List<UUID> collectionsUUIDs) throws Exception{
		
		this.separeteCondition(condition);
		resolverFactory.getMetadataResolver().getItemsFromMetadataAndValue(this.conditions, collectionsUUIDs);
	}
	
	public void getCollectionsFromCondition(String condition) throws Exception{
			
		this.separeteCondition(condition);
		resolverFactory.getMetadataResolver().getCollectionsFromMetadataAndValue(this.conditions, null);
	}

	public void getCommunitiesFromCondition(String condition) throws Exception{
		
		this.separeteCondition(condition);
		resolverFactory.getMetadataResolver().getCommunitiesFromMetadataAndValue(this.conditions, null);
	}
	
	@Override
	public void checkConditions(String oneCondition, MetadataResolver mr) throws Exception{
		EqualCondition.createCondition(oneCondition, this, mr);
		LikeCondition.createCondition(oneCondition, this, mr);
		GraterCondition.createCondition(oneCondition, this, mr);
		LowerCondition.createCondition(oneCondition, this, mr);
	}
	
	
}
