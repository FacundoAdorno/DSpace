package org.dspace.discovery;

import java.util.HashMap;
import java.util.Map;

//TODO eliminar esta clase si no se utiliza
public class StatisticsMutableDiscoverResult extends StatisticsDiscoverResult {

	//El integer representa la posición dentro de la lista original de documentos...
	
	/**
	 * Reemplaza un registro ya existente en los resultados por un nuevo registro
	 * @param searchDocument
	 */
	/**
	private Map<String, Map<String,Integer>> mutableResults = null;
	public void replaceSearchDocument(SearchDocument toReplaceSearchDocument, SearchDocument newSearchDocument) {
		if(mutableResults == null) {
			createMutableResultsMap();
		}
		String statistics_type = toReplaceSearchDocument.getSearchFieldValues("statistics_type").get(0);
        Map<String, Integer> docs = mutableResults.get(statistics_type);
        if(docs!= null) {
        	String uuidToReplace = toReplaceSearchDocument.getSearchFieldValues(StatisticsDiscoverResult.STAT_ID_FIELD).get(0);
        	Integer positionInResults = docs.get(uuidToReplace);
        	if(positionInResults!=null) {
        		//Eliminamos el documento viejo...
        		searchDocuments.get(statistics_type).remove(positionInResults.intValue());
        		//... y ponemos el documento nuevo en la misma posición
        		searchDocuments.get(statistics_type).add(positionInResults, newSearchDocument);
        	}
        }
	}
	
	private void createMutableResultsMap() {
		mutableResults = new HashMap<String, Map<String,Integer>>();
		for (String searchDocumentKey : searchDocuments.keySet()) {
			if(!mutableResults.containsKey(searchDocumentKey)) {
				mutableResults.put(searchDocumentKey, new HashMap<String,Integer>());
			}
			for (int pos=0; pos< searchDocuments.size(); pos++) {
				SearchDocument document = searchDocuments.get(searchDocumentKey).get(pos);
				mutableResults.get(searchDocumentKey).put(document.getSearchFieldValues(StatisticsDiscoverResult.STAT_ID_FIELD).get(0), Integer.valueOf(pos));
			}
		}
	}
	**/
	
//	public class MutableSearchDocument extends SearchDocument{
//		
//		public void replaceFieldValues(String field) {
//			
//		}
//		
//	}
	
}
