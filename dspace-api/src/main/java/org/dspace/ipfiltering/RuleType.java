/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

/**
 * Clase padre con la estructura que las clases que implementan las heurísticas de búsqueda deberian implementar
 * 
 * @author gordo
 *
 */
public abstract class RuleType {
	
	protected static SolrLoggerService solrServer = StatisticsServiceFactory.getInstance().getSolrLoggerService();

	/**
	 * map con las configuraciones definidas en el archivo de configuración
	 */
	protected Map<String, String> settings = new HashMap<String, String>();
	
	protected SolrQuery solrQuery;
	
	/**
	 * Lista parcial de ips detectadas por esta regla
	 */
	protected List<CandidateIP> ipFoundList = new ArrayList<CandidateIP>();

	//Hook methods
	
	protected abstract void getSettings(String prefix) throws MissingArgumentException;
	
	protected abstract void buildQuery();
	
	protected abstract List<CandidateIP> eval() throws SolrServerException;
	
	//Template method
	public List<CandidateIP> run(SolrQuery query, String ruleName) throws SolrServerException, MissingArgumentException
	{
		this.solrQuery = query;
		getSettings(ruleName);
		validateSettings();
		buildQuery();
		return eval();
	}

	/**
	 * Podria pulirse un poco mas, por el momento verifica que no haya valores vacios
	 * 
	 * @throws MissingArgumentException
	 */
	public void validateSettings() throws MissingArgumentException{
		for(Entry<String, String> setting: settings.entrySet())
		{
			if ((setting.getValue() == null) || ("".equals(setting.getValue())))
	        {
				throw new MissingArgumentException(setting.toString());
	        }
		}
	}
	
}
