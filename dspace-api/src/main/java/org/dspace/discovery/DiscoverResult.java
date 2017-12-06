/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import org.dspace.content.DSpaceObject;

import java.util.*;

/**
 * This class represents the result that the discovery search impl returns
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class DiscoverResult extends GenericDiscoverResult {

    
    private List<DSpaceObject> dspaceObjects;
    private Map<String, DSpaceObjectHighlightResult> highlightedResults;


    public DiscoverResult() {
        super();
    	dspaceObjects = new ArrayList<DSpaceObject>();
        highlightedResults = new HashMap<String, DSpaceObjectHighlightResult>();
    }


    public void addDSpaceObject(DSpaceObject dso){
        this.dspaceObjects.add(dso);
    }

    public List<DSpaceObject> getDspaceObjects() {
        return dspaceObjects;
    }
    

    public DSpaceObjectHighlightResult getHighlightedResults(DSpaceObject dso)
    {
        return highlightedResults.get(dso.getHandle());
    }

    public void addHighlightedResult(DSpaceObject dso, DSpaceObjectHighlightResult highlightedResult)
    {
        this.highlightedResults.put(dso.getHandle(), highlightedResult);
    }

    public static final class DSpaceObjectHighlightResult
    {
        private DSpaceObject dso;
        private Map<String, List<String>> highlightResults;

        public DSpaceObjectHighlightResult(DSpaceObject dso, Map<String, List<String>> highlightResults)
        {
            this.dso = dso;
            this.highlightResults = highlightResults;
        }

        public DSpaceObject getDso()
        {
            return dso;
        }

        public List<String> getHighlightResults(String metadataKey)
        {
            return highlightResults.get(metadataKey);
        }
    }

    public void addSearchDocument(DSpaceObject dso, SearchDocument searchDocument){
        String dsoString = SearchDocument.getDspaceObjectStringRepresentation(dso);
        List<SearchDocument> docs = searchDocuments.get(dsoString);
        if(docs == null){
            docs = new ArrayList<SearchDocument>();
        }
        docs.add(searchDocument);
        searchDocuments.put(dsoString, docs);
    }

    /**
     * Returns all the sought after search document values 
     * @param dso the dspace object we want our search documents for
     * @return the search documents list
     */
    public List<SearchDocument> getSearchDocument(DSpaceObject dso){
        String dsoString = SearchDocument.getDspaceObjectStringRepresentation(dso);
        List<SearchDocument> result = searchDocuments.get(dsoString);
        if(result == null){
            return new ArrayList<SearchDocument>();
        }else{
            return result;
        }
    }
}
