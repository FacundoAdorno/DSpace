package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.dspace.content.Item;


public class ConditionResolver extends Resolver{

	private List<Condition> conditions;
	
	public void prepareItemUpdate(String conditions) throws Exception{
		this.separeteCondition(conditions, false);
		factoryManager.getUpdateResolver().updateItems(this.conditions);
	}
	
	public void prepareCollectionUpdate(String conditions) throws Exception{
		this.separeteCondition(conditions, false);
		factoryManager.getUpdateResolver().updateCollections(this.conditions);
	}
	
	public void prepareCommunityUpdate(String conditions) throws Exception{
		this.separeteCondition(conditions, false);
		factoryManager.getUpdateResolver().updateCommunities(this.conditions);
	}
	
	public void getItemsFromCondition(String condition, List<UUID> collectionsUUIDs) throws Exception{
		
		this.separeteCondition(condition, true);
		factoryManager.getMetadataResolver().getItemsFromMetadataAndValue(this.conditions, collectionsUUIDs);
	}
	
	public void getCollectionsFromCondition(String condition) throws Exception{
			
		this.separeteCondition(condition, true);
		factoryManager.getMetadataResolver().getCollectionsFromMetadataAndValue(this.conditions, null);
	}

	public void getCommunitiesFromCondition(String condition) throws Exception{
		
		this.separeteCondition(condition, true);
		factoryManager.getMetadataResolver().getCommunitiesFromMetadataAndValue(this.conditions, null);
	}
	
	public void addCondition(Condition condition){
		this.conditions.add(condition);
	}
	
	private void separeteCondition(String condition, boolean all) throws Exception{
		this.conditions = new ArrayList<Condition>();
		String[] splitConditions = condition.split("\\,");
		MetadataResolver mr = factoryManager.getMetadataResolver();
		for(String oneCondition : splitConditions){
			if (all){
				this.checkAllConditions(oneCondition, mr);
			}else{
				this.checkEqualCondition(oneCondition, mr);
			}
		}		
	}
	
	private void checkAllConditions(String oneCondition, MetadataResolver mr) throws Exception{
		this.checkEqualCondition(oneCondition, mr);
		LikeCondition.createCondition(oneCondition, this, mr);
		GraterCondition.createCondition(oneCondition, this, mr);
		LowerCondition.createCondition(oneCondition, this, mr);
	}
	
	private void checkEqualCondition(String oneCondition, MetadataResolver mr) throws Exception{
		EqualCondition.createCondition(oneCondition, this, mr);		
	}
}
