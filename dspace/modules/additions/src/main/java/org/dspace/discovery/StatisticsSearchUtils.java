package org.dspace.discovery;

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dspace.content.CollectionServiceImpl;
import org.dspace.content.CommunityServiceImpl;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ItemServiceImpl;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult.STAT_TYPES;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationService;
import org.dspace.discovery.configuration.ExtendedDiscoveryConfiguration;
import org.dspace.kernel.ServiceManager;
import org.dspace.services.factory.DSpaceServicesFactory;

public class StatisticsSearchUtils extends SearchUtils {

	/** Cached statistics search service **/
    private static StatisticsSolrServiceImpl statisticsSearchService;
    
    private static final Logger log = Logger.getLogger(StatisticsSearchUtils.class);
    
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
    
    /**
     * Se generan expresiones "field:value", separados por op lógico "OR", a partir de la definición de la propiedad "defaultQueryFields" en la configuración de Discovery...
     */
	public static String generateDefaultFields(String query, DSpaceObject scope) {
		StringBuilder builder = new StringBuilder();
		if(query != null && !query.trim().equals("")) {
			ExtendedDiscoveryConfiguration configuration  = (ExtendedDiscoveryConfiguration)getDiscoveryConfiguration(scope);
			if(configuration.getDefaultQueryFields().size() > 0) {
				Iterator<String> allFields = configuration.getDefaultQueryFields().iterator();
				while(allFields.hasNext()) {
					String queryField = allFields.next();
					builder.append(queryField);
					builder.append(":");
					//Creamos una expresión regular con el término de búsqueda...
					builder.append("/.*"); builder.append(query); builder.append(".*/");
					if(allFields.hasNext()) {
						builder.append(" OR ");
					}
				}
			}
			return builder.toString();
		}
		return null;
	}
	
	
	public static DSpaceObject getDSOByStatistic(SearchDocument document, Context context) throws SQLException {
		java.util.List<String> statTypeList = document.getSearchFields().get("statistics_type");
		java.util.List<String> dsoTypeList;
		java.util.List<String> idList;
		if(statTypeList == null || statTypeList.size() == 0) {
			log.warn("The statistics with uid=" + document.getSearchFields().get("uid"));
			return null;
		} else {
			String statisticType = statTypeList.get(0);
			if(statisticType.equals(STAT_TYPES.SEARCH.text())) {
				dsoTypeList = document.getSearchFields().get("scopeType");
		    	idList = document.getSearchFields().get("scopeId");
			} else {
				//Otherwise, if "wokflow" other "view"
				dsoTypeList = document.getSearchFields().get("type");
				idList = document.getSearchFields().get("id");
			}
		}
    	//Obtenemos el handle del DSO asociado a esta estadística
    	DSpaceObject result = null;
    	if(idList != null && dsoTypeList!= null && idList.size() > 0 && dsoTypeList.size() > 0) {
    		String dsoID = idList.get(0);
    		int dsoType = Integer.parseInt(dsoTypeList.get(0));
    		
    		switch (dsoType) {
			case Constants.BITSTREAM:
				//TODO Agregamos el handle del padre del bitstream??
				break;
			case Constants.ITEM:
				ItemServiceImpl itemService = (ItemServiceImpl)ContentServiceFactory.getInstance().getDSpaceObjectService(dsoType);
				result = itemService.findByIdOrLegacyId(context, dsoID);
				break;
			case Constants.COLLECTION:
				CollectionServiceImpl collectionService = (CollectionServiceImpl)ContentServiceFactory.getInstance().getDSpaceObjectService(dsoType);
				result = collectionService.findByIdOrLegacyId(context, dsoID);
				break;
			case Constants.COMMUNITY:
				CommunityServiceImpl communityService = (CommunityServiceImpl)ContentServiceFactory.getInstance().getDSpaceObjectService(dsoType);
				result = communityService.findByIdOrLegacyId(context, dsoID);
				break;
			default:
				break;
			}
    	}
    	return result;
	}
    
}
