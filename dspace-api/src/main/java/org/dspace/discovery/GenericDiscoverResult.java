/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dspace.content.DSpaceObject;

public class GenericDiscoverResult {
	protected long totalSearchResults;
    protected int start;
    protected Map<String, List<FacetResult>> facetResults;
    /** A map that contains all the documents sougth after, the key is a string representation of the DSpace object */
    protected Map<String, List<SearchDocument>> searchDocuments;
    protected int maxResults = -1;
    protected int searchTime;
    protected String spellCheckQuery;
    
    GenericDiscoverResult(){
    	facetResults = new LinkedHashMap<String, List<FacetResult>>();
        searchDocuments = new LinkedHashMap<String, List<SearchDocument>>();
        
    }
    
    public long getTotalSearchResults() {
        return totalSearchResults;
    }

    public void setTotalSearchResults(long totalSearchResults) {
        this.totalSearchResults = totalSearchResults;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getSearchTime()
    {
        return searchTime;
    }

    public void setSearchTime(int searchTime)
    {
        this.searchTime = searchTime;
    }

    public void addFacetResult(String facetField, FacetResult ...facetResults){
        List<FacetResult> facetValues = this.facetResults.get(facetField);
        if(facetValues == null)
        {
            facetValues = new ArrayList<FacetResult>();
        }
        facetValues.addAll(Arrays.asList(facetResults));
        this.facetResults.put(facetField, facetValues);
    }

    public Map<String, List<FacetResult>> getFacetResults() {
        return facetResults;
    }

    public List<FacetResult> getFacetResult(String facet){
        return facetResults.get(facet) == null ? new ArrayList<FacetResult>() : facetResults.get(facet);
    }
    
    public String getSpellCheckQuery() {
        return spellCheckQuery;
    }

    public void setSpellCheckQuery(String spellCheckQuery) {
        this.spellCheckQuery = spellCheckQuery;
    }
    
    public static final class FacetResult{
        private String asFilterQuery;
        private String displayedValue;
        private String authorityKey;
        private String sortValue;
        private long count;

        public FacetResult(String asFilterQuery, String displayedValue, String authorityKey, String sortValue, long count) {
            this.asFilterQuery = asFilterQuery;
            this.displayedValue = displayedValue;
            this.authorityKey = authorityKey;
            this.sortValue = sortValue;
            this.count = count;
        }

        public String getAsFilterQuery() {
            return asFilterQuery;
        }

        public String getDisplayedValue() {
            return displayedValue;
        }

        public String getSortValue()
        {
            return sortValue;
        }
        
        public long getCount() {
            return count;
        }

        public String getAuthorityKey()
        {
            return authorityKey;
        }

        public String getFilterType()
        {
            return authorityKey != null?"authority":"equals";
        }
    }
    
    /**
     * This class contains values from the fields searched for in DiscoveryQuery.java
     */
    public static class SearchDocument{
        private Map<String, List<String>> searchFields;

        public SearchDocument() {
            this.searchFields = new LinkedHashMap<String, List<String>>();
        }

        public void addSearchField(String field, String ...values){
            List<String>searchFieldValues = searchFields.get(field);
            if(searchFieldValues == null){
                searchFieldValues = new ArrayList<String>();
            }
            searchFieldValues.addAll(Arrays.asList(values));
            searchFields.put(field, searchFieldValues);
        }

        public Map<String, List<String>> getSearchFields() {
            return searchFields;
        }

        public List<String> getSearchFieldValues(String field){
            if(searchFields.get(field) == null)
                return new ArrayList<String>();
            else
                return searchFields.get(field);
        }
        
        //TODO: por ahi conviene crear una subclase de SearchDocument que permita cambiar los valores en vez de cambiarlos directamente
        /**
         * Se reemplaza un campo del documento por un conjunto nuevo de valores. El conjunto no debería estar vacío.
         **/
    	public void replaceFieldValues(String field, List<String> newValues) {
    		if(searchFields.get(field) != null && newValues != null & !newValues.isEmpty()) {
    			searchFields.remove(field);
    			searchFields.put(field, newValues);
    		}
    	}

        public static String getDspaceObjectStringRepresentation(DSpaceObject dso){
            return dso.getType() + ":" + dso.getID();
        }
    }
}
