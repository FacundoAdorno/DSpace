package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

public class Update {
	
	protected Context c = TransactionManager.getContext();

	protected void update(List<MetadataValue> mvList, MetadataField metadataField, String newValue, String regex, boolean updateAll, DSpaceObject dso, boolean preview) throws SQLException, AuthorizeException{
		List<String> newValues = new ArrayList<String>();
		boolean anyChange = false;
		for(MetadataValue mv: mvList){
			if(anyChange && !updateAll){
				newValues.add(mv.getValue());
				continue;
			}
			Pattern pat = Pattern.compile(regex);
			//escapeo el string
			String quoteReplacement = Matcher.quoteReplacement(mv.getValue());
			Matcher mat = pat.matcher(quoteReplacement);
			StringBuffer sb = new StringBuffer();
			if(updateAll){
				while(mat.find()){
					mat.appendReplacement(sb, newValue);
				}
			}else{
				if(mat.find()){
					mat.appendReplacement(sb, newValue);
				}
			}			
			mat.appendTail(sb);
			newValues.add(sb.toString());
			if(sb.toString() != mv.getValue()){
				anyChange = true;
			}
		}
		if(anyChange && preview){
			doModifyPreview(dso, metadataField, newValues);
		}else if(anyChange && !preview){
			doUpdate(dso, metadataField, newValues);
		}
		
	}
	
	public void addPreview(List<DSpaceObject> DSOs, List<Condition> conditions, boolean updateAll){
		List<DSpaceObjectPreview> previews = new ArrayList<DSpaceObjectPreview>();
		for(DSpaceObject dso:  DSOs){
			for(Condition condition: conditions){
				previews.add(new DSpaceObjectPreview(dso.getHandle(), condition.getMetadataField(), "-", condition.getMetadataValue()));
			}
		}
		PreviewManager.addPreview(previews);
	}
	
	public void deletePreview(List<DSpaceObject> DSOs, List<Condition> conditions, String updateAll){
		List<DSpaceObjectPreview> previews = new ArrayList<DSpaceObjectPreview>();
		for(DSpaceObject dso:  DSOs){
			for(Condition condition: conditions){
				List<MetadataValue> mvList = getMetadataValueList(dso, condition.getMetadataField()); 
				for(MetadataValue mv: mvList){
					previews.add(new DSpaceObjectPreview(dso.getHandle(), condition.getMetadataField(), mv.getValue(), "-" ));
				}				
			}
		}
		PreviewManager.addPreview(previews);
	}
	
	public void modifyPreview(List<DSpaceObject> DSOs, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(DSpaceObject dso:  DSOs){
			for(Condition condition: conditions){
				List<MetadataValue> mvList = getMetadataValueList(dso, condition.getMetadataField());
				update(mvList, condition.getMetadataField(), condition.getMetadataValue(), condition.getRegex(), updateAll, dso, true);			
			}
		}
	}
	
	public void doModifyPreview(DSpaceObject dso, MetadataField metadataField, List<String> newValues){		
		List<MetadataValue> mvList = getMetadataValueList(dso, metadataField);
		for(int i = 0; i< newValues.size(); i++){
			String currentNewValue = newValues.get(i);
			MetadataValue currentMV = mvList.get(i);
			if(!currentNewValue.equals(currentMV.getValue())){
				PreviewManager.addPreview(new DSpaceObjectPreview(dso.getHandle(), metadataField, currentMV.getValue(), currentNewValue));
			}
		}
	}
	
	protected List<MetadataValue> getMetadataValueList(DSpaceObject dso, MetadataField metadataField){
		return new ArrayList<MetadataValue>();
	}

	protected void doUpdate( DSpaceObject item, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{}

	public void modify(DSpaceObject item, List<Condition> conditions, boolean updateAll)
			throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub
	}

	public void add(DSpaceObject dso, List<Condition> conditions, boolean updateAll)
			throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub
	}

	public void delete(DSpaceObject item, List<Condition> conditions, boolean updateAll)
			throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub
	}
	
}