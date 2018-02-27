package org.dspace.discovery.configuration;

/**
 * Un facet filter combinado es una tipo de facet utilizado para combinar varias listas de resultados de facets en un único
 * facet. Por ejemplo, en el core de Statistics, los campos 'type' y 'scopeType' representan los tipos de DSO que determina el
 * scope de los registros estadísticos del tipo 'view' y 'search' respectivamente. Si se quisiera hacer un facet único
 * a partir de estos dos fields distintos, entonces hay que utilizar este tipo de filtro.
 * 
 */
public class StatisticsDiscoveryCombinedFilterFacet extends DiscoverySearchFilterFacet {
	
	public static final String FILTER_TYPE_COMBINED_FACET = "combined";
	
	@Override
	public String getFilterType() {
		return FILTER_TYPE_COMBINED_FACET;
	}
}
