package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

public class Update {
	
	protected Context c = TransactionManager.getContext();

	protected void update(List<MetadataValue> mvList, MetadataField metadataField, String newValue, String regex, boolean updateAll, DSpaceObject dso) throws SQLException, AuthorizeException{
		List<String> newValues = new ArrayList<String>();
		boolean anyChange = false;
		for(MetadataValue mv: mvList){
			if(anyChange && !updateAll){
				newValues.add(mv.getValue());
				continue;
			}
			Pattern pat = Pattern.compile(regex);
			Matcher mat = pat.matcher(mv.getValue());
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
		if(anyChange){
			doUpdate(dso, metadataField, newValues);
		}
		
	}
	
	protected void doUpdate( DSpaceObject item, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{}
	
}