/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.configuration;

import java.util.ArrayList;
import java.util.List;

public class ExtendedDiscoveryConfiguration extends DiscoveryConfiguration {
	/** The search filters which can be selected on the search page**/
    private List<DiscoveryMultipleSearchFilter> multipleSearchFilters = new ArrayList<DiscoveryMultipleSearchFilter>();
    
    private List<String> defaultQueryFields = new ArrayList<String>();

	public List<DiscoveryMultipleSearchFilter> getMultipleSearchFilters() {
		return multipleSearchFilters;
	}

	public void setMultipleSearchFilters(List<DiscoveryMultipleSearchFilter> multipleSearchFilters) {
		this.multipleSearchFilters = multipleSearchFilters;
	}
    
	public List<String> getDefaultQueryFields() {
		return defaultQueryFields;
	}

	public void setDefaultQueryFields(List<String> defaultQueryFields) {
		this.defaultQueryFields = defaultQueryFields;
	}

	@Override
    public void afterPropertiesSet() throws Exception{
    	for (DiscoveryMultipleSearchFilter discoveryMultipleSearchFilter : multipleSearchFilters) {
			this.getSearchFilters().addAll(discoveryMultipleSearchFilter.convertToFilters());
		}
		super.afterPropertiesSet();
    }
}
