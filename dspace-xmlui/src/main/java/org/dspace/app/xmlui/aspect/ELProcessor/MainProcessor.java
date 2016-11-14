package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELProcessor;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.Context;

public class MainProcessor {
	
	private static InstanciadorEL instanciador;
	private static ELProcessor processor;
	private static Context context;
	private static List<Item> items = new ArrayList<Item>();
	private static List<Collection> collections = new ArrayList<Collection>();
	private static List<Community> communities = new ArrayList<Community>();
	
	public void process(String query, Context context) throws SQLException {
		setContext(context);
		if(query==null){
			return;
		}
		if(processor==null){
			processor=getInstanciador().instanciar(context);
		}
		query = this.prepareQuery(query);
		processor.eval(query);
		
		this.setResults();
		
	}
	
	private String prepareQuery(String query){
		//me quedo con los parametros, es decir con lo que esta entre parentesis
		String[] splitQuery = query.split("\\(");
		String parameter = splitQuery[1];
		parameter = parameter.substring(0, parameter.length()-1);
		//el guion medio (-) me va a diferenciar entre parametros de seleccion y transformacion
		String[] splitParameter = parameter.split("\\-");
		if(splitParameter.length == 2){
			//hay parametros de transformacion
			parameter = splitParameter[0].trim() +"','"+ splitParameter[1].trim();
		}
		return splitQuery[0] +"('"+ parameter.trim() +"')";
	}
	
	private void setResults(){
		SelectionPage.setItemsResult(items);
		SelectionPage.setCollectionsResult(collections);
		SelectionPage.setCommunitiesResult(communities);
	}

	public static InstanciadorEL getInstanciador() {
		if(instanciador==null){
			instanciador = new InstanciadorEL();
		}
		return instanciador;
	}

	public static void setInstanciador(InstanciadorEL instanciador) {
		MainProcessor.instanciador = instanciador;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		MainProcessor.context = context;
	}
	
	public static void addResultItems(List<Item> result){
		items.addAll(result);
	}
	
	public static void addResultItem(Item result){
		items.add(result);
	}
	
	public static void addResultCollection(Collection result){
		collections.add(result);
	}
	
	public static void addResultCommunity(Community result){
		communities.add(result);
	}
	
	public static void addResultCollections(List<Collection> result){
		collections.addAll(result);
	}
	
	public static void addResultCommunities(List<Community> result){
		communities.addAll(result);
	}
	
	
	
	
}
