package org.dspace.discovery.exporter;

import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;

public interface StatisticsResponseTransformer {
	
	/**
	 * Hook para realizar alguna operación antes de realizar la consulta a Solr (por ejemplo, setear el tipo de respuesta que queremos 
	 * que retorne la consulta a Solr, evitar la impresión de ciertos campos, etc.).
	 * @param query		es el objeto que contiene los parametros de la consulta solr
	 */
	public void beforeQuery(DiscoverQuery query);
	
	
	/**
	 * Hook donde se realizarán todas las modificaciones al resultado devuelto a cada documento resultante de la consulta Discovery, por 
	 * ejemplo, transformaciones en el valor (p.e. en vez de mostrar constante de comunidad "4" mostramos el label "COMMUNITY").
	 * @param document	representa uno de los documentos resultantes de la consulta a Solr. Tener en cuenta que debe ser un SearchDocument mutable...
	 */
	public void afterQuery(SearchDocument document);
}
