package org.dspace.discovery;

import java.util.Date;

import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters.SORT;

/**
 * Clase para representar un campo de facet del tipo <i>date</i> en Solr desde Discovery. Ver https://wiki.apache.org/solr/SimpleFacetParameters#Date_Faceting_Parameters
 * @author facundo
 *
 */
public class DiscoveryDateFacetField extends DiscoverFacetField {
	
	/**
	 * Representa el gap o margen de fecha entre los valores del facet, como por ejemplo: +1DAY, +1MONTH, +1YEAR, etc.
	 * Ver en https://wiki.apache.org/solr/SimpleFacetParameters#facet.date.gap en la documentacion Solr.
	 */
	private String gap;
	private Date startDate;
	private Date endDate;
	private int minCount;
	
	public String getGap() {
		return gap;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public int getMinCount() {
		return minCount;
	}
	
	public DiscoveryDateFacetField(String field, String type, int limit, SORT sortOrder, String gap, Date startDate, Date endDate, int minCount) {
		super(field, type, limit, sortOrder);
		this.gap = gap;
		this.startDate = startDate;
		this.endDate = endDate;
		this.minCount = minCount;
	}
	
	public DiscoveryDateFacetField(String field, String type, int limit, DiscoveryConfigurationParameters.SORT sortOrder, int offset, String gap, Date startDate, Date endDate, int minCount) {
        super(field, type, limit, sortOrder,offset);
        this.gap = gap;
		this.startDate = startDate;
		this.endDate = endDate;
		this.minCount = minCount;
    }

    public DiscoveryDateFacetField(String field, String type, int limit, DiscoveryConfigurationParameters.SORT sortOrder, String prefix, String gap, Date startDate, Date endDate, int minCount) {
    	super(field, type, limit, sortOrder, prefix);
    	this.gap = gap;
		this.startDate = startDate;
		this.endDate = endDate;
		this.minCount = minCount;
    }

    public DiscoveryDateFacetField(String field, String type, int limit, DiscoveryConfigurationParameters.SORT sortOrder, String prefix, int offset, String gap, Date startDate, Date endDate, int minCount) {
        super(field, type, limit, sortOrder, prefix, offset);
        this.gap = gap;
		this.startDate = startDate;
		this.endDate = endDate;
		this.minCount = minCount;
    }
}
