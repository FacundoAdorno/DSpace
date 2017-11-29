package org.dspace.discovery;

import org.apache.lucene.analysis.standard.std34.StandardTokenizerImpl34;
import org.dspace.content.DSpaceObject;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationService;
import org.dspace.kernel.ServiceManager;
import org.dspace.services.factory.DSpaceServicesFactory;

public class StatisticsSearchUtils extends SearchUtils {

	/** Cached statistics search service **/
    private static StatisticsSolrServiceImpl statisticsSearchService;
    
    private static String statisticsConfigurationBeanName = "statisticsDiscoveryConfiguration";


    public static StatisticsSolrServiceImpl getStatisticsSearchService() {
    	 if(statisticsSearchService ==  null){
             org.dspace.kernel.ServiceManager manager = DSpaceServicesFactory.getInstance().getServiceManager();
             statisticsSearchService = manager.getServiceByName(StatisticsSolrServiceImpl.class.getName(),StatisticsSolrServiceImpl.class);
         }
         return statisticsSearchService;
    }
	
    public static DiscoveryConfigurationService getConfigurationService() {
        ServiceManager manager = DSpaceServicesFactory.getInstance().getServiceManager();
        return manager.getServiceByName(StatisticsSearchUtils.statisticsConfigurationBeanName, DiscoveryConfigurationService.class);
    }
    
    public static DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject dso){
        DiscoveryConfigurationService configurationService = getConfigurationService();

        DiscoveryConfiguration result = null;
        if(dso == null){
            result = configurationService.getMap().get("site");
        }else{
            result = configurationService.getMap().get(dso.getHandle());
        }

        if(result == null){
            //No specific configuration, get the default one
            result = configurationService.getMap().get("default");
        }

        return result;
    }
    
}
