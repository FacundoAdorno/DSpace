package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.dspace.content.Item;


public class ConditionManager extends Manager{

	private List<Condition> conditions;
	
	
	public void getItemsFromCondition(String condition, List<UUID> collectionsUUIDs) throws Exception{
		
		this.separeteCondition(condition);
		metadataManager.getItemsFromMetadataAndValue(this.conditions, collectionsUUIDs);
	}
	
	public void getCollectionsFromCondition(String condition) throws Exception{
			
		this.separeteCondition(condition);
		metadataManager.getCollectionsFromMetadataAndValue(this.conditions, null);
	}

	public void getCommunitiesFromCondition(String condition) throws Exception{
		
		this.separeteCondition(condition);
		metadataManager.getCommunitiesFromMetadataAndValue(this.conditions, null);
	}
	
	public void addCondition(Condition condition){
		this.conditions.add(condition);
	}
	
	private void separeteCondition(String condition) throws Exception{
		this.conditions = new ArrayList<Condition>();
		String[] splitConditions = condition.split("\\,");
		for(String oneCondition : splitConditions){
			EqualCondition.createCondition(oneCondition, this);
			LikeCondition.createCondition(oneCondition, this);
			GraterCondition.createCondition(oneCondition, this);
			LowerCondition.createCondition(oneCondition, this);
		}		
	}	
}
