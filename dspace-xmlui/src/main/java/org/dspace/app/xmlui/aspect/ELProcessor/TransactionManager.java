package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;

public class TransactionManager {
	
	private static Context context;	
	
	public static Context getContext(){
		return context;
	}
	
	public static void roolback(){
		context.abort();
	}
	
	public static void commitAndClose() throws SQLException{
		context.complete();
		context = null;
	}
	
	public static void commit() throws SQLException{
		context.commit();
	}

	public static void setContext(Context newContext) {		
		context = newContext;
	}

	public static DSpaceObject reload(DSpaceObject entity) throws SQLException {
		return context.reloadEntity(entity);
		
	}
	

}
