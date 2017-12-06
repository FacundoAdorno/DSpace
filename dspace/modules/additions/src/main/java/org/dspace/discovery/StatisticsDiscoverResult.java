package org.dspace.discovery;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;

public class StatisticsDiscoverResult extends GenericDiscoverResult{

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
	
}
