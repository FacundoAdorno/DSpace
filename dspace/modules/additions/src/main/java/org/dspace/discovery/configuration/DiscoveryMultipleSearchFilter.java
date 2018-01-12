/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.configuration;

import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * Permite declarar múltiples DiscoverySearchFilters en uno solo
 * Los filtros deben ser del mismo tipo (text, date, etc.)
 * @author facundo
 *
 */
public class DiscoveryMultipleSearchFilter {

    protected String filterName;
    /**
     * Cada valor de la lista representa un DiscoverySearchFilter independiente...
     */
    protected List<String> indexMedatadaFields;
    protected String type = DiscoveryConfigurationParameters.TYPE_TEXT;
    public static final String FILTER_TYPE_DEFAULT = "default";

    public String getFilterName() {
        return filterName;
    }

    @Required
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    //TODO por cada indexFieldName tiene que crearse un message con el prefijo xmlui.ArtifactBrowser.SimpleSearch.filter. + valor de este campo
    
    public List<String> getIndexMedatadaFields() {
        return indexMedatadaFields;
    }

    @Required
    public void setIndexMedatadaFields(List<String> indexMetadataFields) {
        this.indexMedatadaFields = indexMetadataFields;
    }

    public String getType() {
        return type;
    }

    public void setType(String type){
        if(type.equalsIgnoreCase(DiscoveryConfigurationParameters.TYPE_TEXT))
        {
            this.type = DiscoveryConfigurationParameters.TYPE_TEXT;
        } else
        if(type.equalsIgnoreCase(DiscoveryConfigurationParameters.TYPE_DATE))
        {
            this.type = DiscoveryConfigurationParameters.TYPE_DATE;
        }else{
            this.type = type;
        }
    }

    public String getFilterType(){
        return FILTER_TYPE_DEFAULT;
    }
    
    /**
     * Convierte cada valor de la lista en un conjunto de {@code DiscoverySearchFilter} independientes, en los que su campo 'metadataFields' está vacío,
     * de tal forma que no se trate de indexar ningun metadata nuevo en los documentos Solr cuando se utilice este filtro por los servicios de indexación...
     * 
     * @return {@code ArrayList<DiscoverySearchFilter>} una lista de filtros independientes
     */
    public ArrayList<DiscoverySearchFilter> convertToFilters(){
    	ArrayList<DiscoverySearchFilter> filters = new ArrayList<DiscoverySearchFilter>();
    	for (String indexMetadataField : this.getIndexMedatadaFields()) {
			DiscoverySearchFilter searchFilter = new DiscoverySearchFilter();
			searchFilter.setMetadataFields(new ArrayList<String>());
			//Creamos una lista de metadatos vacía, ya que no nos interesa utilizarlas para indexar en el core Solr...
			searchFilter.setMetadataFields(new ArrayList<String>());
			searchFilter.setIndexFieldName(indexMetadataField);
			try {
				searchFilter.setType(this.getType());
			} catch (DiscoveryConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filters.add(searchFilter);
		}
    	return filters;
    }

}
