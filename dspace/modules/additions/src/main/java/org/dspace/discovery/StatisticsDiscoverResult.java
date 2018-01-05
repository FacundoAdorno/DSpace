package org.dspace.discovery;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;

public class StatisticsDiscoverResult extends GenericDiscoverResult{

    public static enum STAT_TYPES{
    	SEARCH("search"), WFW("workflow"), VIEW("view");
    	
    	private String type;
    	
    	STAT_TYPES(String type){
    		this.type = type;
    	}
    	
    	public String text() {
    		return this.type;
    	}
    }
	
	//Se agregan documentos agregándolos por tipo de búsqueda por ahora...
	//Conviene ver si es mejor agregar por handle del objeto asociado a la estadística... Hay casos en que no hay handle asociado (por ejemplo algunas estadisticas de busqueda[search])
	public void addSearchDocument(SearchDocument searchDocument){
        String statistics_type = searchDocument.getSearchFieldValues("statistics_type").get(0);
        List<SearchDocument> docs = searchDocuments.get(statistics_type);
        if(docs == null){
            docs = new ArrayList<SearchDocument>();
        }
        docs.add(searchDocument);
        searchDocuments.put(statistics_type, docs);
    }
	
	/**
	 * Retorna todos los resultados de estadísticas en un único array
	 * @return ArrayList<SearchDocument>
	 */
	public ArrayList<SearchDocument> getAllResults(){
		ArrayList<SearchDocument> results = new ArrayList<SearchDocument>();
		for (List<SearchDocument> documents : searchDocuments.values()) {
			results.addAll(documents);
		}
		return results;
	}
	
	public ArrayList<SearchDocument> getResultsByType(STAT_TYPES type){
		ArrayList<SearchDocument> result = new ArrayList<SearchDocument>();
		if(searchDocuments.containsKey(type.text())) {
			result.addAll(searchDocuments.get(type.text()));
		}
		return result;
	}
	
}
