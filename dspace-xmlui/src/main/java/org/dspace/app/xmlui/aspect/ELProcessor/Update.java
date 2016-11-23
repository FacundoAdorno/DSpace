package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
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

	protected void update(List<MetadataValue> mvList, MetadataField metadataField, String newValue, String regex, boolean updateAll, DSpaceObject dso) throws SQLException, AuthorizeException{
		for(MetadataValue mv: mvList){
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
			Context c = TransactionManager.getContext();
			doUpdate(c, dso, metadataField, sb.toString());
		}		
	}
	
	protected void doUpdate(Context c, DSpaceObject item, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{}
	
}