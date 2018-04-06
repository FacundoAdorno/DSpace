package org.dspace.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dspace.content.DSpaceObject;
import org.dspace.discovery.GenericDiscoverResult.FacetResult;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.discovery.configuration.StatisticsDiscoveryCombinedFilterFacet;

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
    
    
    
    /**
     * Identificar único de acceso al registro estadístico
     */
    public static String STAT_ID_FIELD = "uuid";
    
    protected Map<String, List<DateRangeFacetResult>> rangeDatefacetResults;
	
    public StatisticsDiscoverResult(){
    	super();
    	this.rangeDatefacetResults = new LinkedHashMap<String, List<DateRangeFacetResult>>();
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
	
	/**
	 * Retorna una lista combinada a partir de los resultados de una consulta Discovery y un filtro del tipo "combined", es decir, unifica en una única
	 * lista (sin repetidos) distintos resultados de los facets vinculados al filtro del tipo "combined".
	 * 
	 * @param queryResults	es el objeto que contiene los resultados de la consulta Discovery
	 * @param facetField	es el filtro del tipo "combined"
	 * @return una lista combinada de resultados de facets.
	 */
	public static java.util.List<FacetResult> getCombinedFacetValues(StatisticsDiscoverResult queryResults, StatisticsDiscoveryCombinedFilterFacet facetField){
		java.util.HashMap<String,StatisticsDiscoverResult.FacetResult> uniqueListOfValues = new HashMap<String,StatisticsDiscoverResult.FacetResult>();
		for (String metadataField : facetField.getMetadataFields()) {
			for (FacetResult facetResult : queryResults.getFacetResult(metadataField)) {
				String facetValue = facetResult.getDisplayedValue();
				//Combinamos los resultados de facet en uno nuevo, ya que no puedo modificar el existente...
				if(uniqueListOfValues.containsKey(facetValue)){
					FacetResult inListFacet = uniqueListOfValues.get(facetValue);
					uniqueListOfValues.remove(facetValue);
					uniqueListOfValues.put(facetValue, 
							new FacetResult(inListFacet.getAsFilterQuery(), inListFacet.getDisplayedValue(), inListFacet.getAuthorityKey(), inListFacet.getSortValue(), (inListFacet.getCount() + facetResult.getCount())));
				} else {
					uniqueListOfValues.put(facetValue, facetResult);
				}
			} 
		}
		
		return new ArrayList<FacetResult>(uniqueListOfValues.values());
		
	}
	
	public void addDateRangeFacetResult(String dateRangeFacetField, DateRangeFacetResult ...dateRangeFacetResults){
        List<DateRangeFacetResult> dateRangeFacetValues = this.rangeDatefacetResults.get(dateRangeFacetField);
        if(dateRangeFacetValues == null)
        {
            dateRangeFacetValues = new ArrayList<DateRangeFacetResult>();
        }
        dateRangeFacetValues.addAll(Arrays.asList(dateRangeFacetResults));
        this.rangeDatefacetResults.put(dateRangeFacetField, dateRangeFacetValues);
    }

    public Map<String, List<DateRangeFacetResult>> getDateRangeFacetResults() {
        return rangeDatefacetResults;
    }

    public List<DateRangeFacetResult> getDateRangeFacetResult(String dateRangeFacetName){
        return rangeDatefacetResults.get(dateRangeFacetName) == null ? new ArrayList<DateRangeFacetResult>() : rangeDatefacetResults.get(dateRangeFacetName);
    }
	
	public static final class DateRangeFacetResult{
        private String asFilterQuery;
        private String displayedValue;
        private long count;
        private String gap;
        private String startDate;
        private String endDate;

        public DateRangeFacetResult(String asFilterQuery, String displayedValue, long count, String gap, String startDate, String endDate) {
            this.asFilterQuery = asFilterQuery;
            this.displayedValue = displayedValue;
            this.count = count;
            this.gap = gap;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getAsFilterQuery() {
            return asFilterQuery;
        }

        public String getDisplayedValue() {
            return displayedValue;
        }
        
        public long getCount() {
            return count;
        }
        
        public String getGap() {
        	return this.gap;
        }
        
        public String getStartDate() {
        	return this.startDate;
        }
        
        public String getEndDate() {
        	return this.endDate;
        }
        
    }
	
}
