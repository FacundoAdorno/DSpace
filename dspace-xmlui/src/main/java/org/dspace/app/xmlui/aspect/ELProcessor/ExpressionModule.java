package org.dspace.app.xmlui.aspect.ELProcessor;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELProcessor;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.Context;

public class ExpressionModule {
	
	public void process(String query, Context context) throws SQLException {
		TransactionManager.setContext(context);
		if(query==null || query.equals("")){
			return;
		}
		
		ELProcessor processor=InstanciadorEL.getProcessor(context);
		
		query = this.prepareQuery(query);
		try{
			processor.eval(query);
		}
		catch(Exception e){
			TransactionManager.roolback();
			throw e;
		}	
	}
	
	public void executeTransformation(Context context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, AuthorizeException{
		TransactionManager.setContext(context);
		try{
			TransformationAction.executeUpdate();
		}
		catch(Exception e){
			TransactionManager.roolback();
			throw e;
		}
	}
	
	private String prepareQuery(String query){
		//me quedo con los parametros, es decir con lo que esta entre parentesis
		String[] splitQuery = query.split("\\(");
		String parameter = splitQuery[1].trim();
		parameter = parameter.split("\\)").length == 2 ? parameter.split("\\)")[0] : "";
		//el guion medio (-) me va a diferenciar entre parametros de seleccion y transformacion
		String[] splitParameter = parameter.split("\\-");
		if(splitParameter.length == 2){
			//hay parametros de transformacion
			parameter = splitParameter[0].trim() +"','"+ splitParameter[1].trim();
		}
		return splitQuery[0] +"('"+ parameter.trim() +"')";
	}	
	
}
