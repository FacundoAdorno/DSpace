package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Collection;
import org.dspace.content.Community;

public class ResultContainer{
	
	private static boolean wasSet = false;
	private static List<DSpaceObject> DSOs = new ArrayList<DSpaceObject>();
	private static List<DSpaceObjectPreview> resultToShow = new ArrayList<DSpaceObjectPreview>();
	
	public static List<DSpaceObjectPreview> getResultsToShow(){
		if(resultToShow.isEmpty() && wasSet){
			SelectionPage.showNoResult();
		}
		return resultToShow;
	}
	
	public static void cleanResults(){
		DSOs = new ArrayList<DSpaceObject>();
		cleanPreviewResult();
		wasSet = false;
	}
	
	public static void cleanPreviewResult(){
		resultToShow = new ArrayList<DSpaceObjectPreview>();
	}
	
	public static void addResultsToShow(List<DSpaceObjectPreview> results){
		wasSet = true;
		resultToShow = results;
	}
	
	public static void addResultToShow(DSpaceObjectPreview result){
		wasSet = true;
		resultToShow.add(result);
	}
	
	//Estos metodos van a ser llamados solo en consultas de seleccion
	//Por lo tanto la info que muestro de los DSOs seleccionados
	//es siempre la misma, su titulo y handle
	public static void addResults(List<DSpaceObject> dsos){
		wasSet = true;
		for(DSpaceObject dso: dsos){
			addResult(dso);
		}
	}
	
	public static void addItems(List<Item> items){
		wasSet = true;
		for(DSpaceObject dso: items){
			addResult(dso);
		}
	}
	
	public static void addCollections(List<Collection> collections){
		wasSet = true;
		for(DSpaceObject dso: collections){
			addResult(dso);
		}
	}
	
	public static void addCommunities(List<Community> communities){
		wasSet = true;
		for(DSpaceObject dso: communities){
			addResult(dso);
		}
	}
	
	public static void addResult(DSpaceObject dso){
		wasSet = true;
		SelectionPage.showItemSelectionMessage();
		resultToShow.add(new DSpaceObjectPreview(dso.getHandle(), "dc.title", dso.getName(), "-"));
		DSOs.add(dso);
	}
	
	public static List<DSpaceObject> getDSOs(){
		return DSOs;
	}	
	
}
