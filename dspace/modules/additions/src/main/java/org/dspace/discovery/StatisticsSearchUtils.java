package org.dspace.discovery;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
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
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.configuration.ExtendedDiscoveryConfiguration;
import org.dspace.kernel.ServiceManager;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

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
	
	/**
	 * Construye una expresión regular para obtener una matcheo exacto para el string pasado como parámetro
	 * @return	expresión regular para un "exact match" construida a partir del parámetro recibido
	 */
	public static String getExactMatchFilter(String targetToexactMatch) {
		return "/" + targetToexactMatch +"/";
	}
	
	
	public static DSpaceObject getDSOByStatistic(SearchDocument document, Context context) throws SQLException {
		java.util.List<String> statTypeList = document.getSearchFields().get("statistics_type");
		java.util.List<String> dsoTypeList;
		java.util.List<String> idList;
		if(statTypeList == null || statTypeList.size() == 0) {
			log.warn("The statistics with uid=" + document.getSearchFields().get("uid") + "does not have a 'statistics_type' vinculated.");
			return null;
		} else {
			String statisticType = statTypeList.get(0);
			if(statisticType.equals(STAT_TYPES.SEARCH.text())) {
				dsoTypeList = document.getSearchFields().get("scopeType");
		    	idList = document.getSearchFields().get("scopeId");
			} else {
				//Otherwise, if "wokflow" or "view"
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
	
	public static DiscoverySearchFilter getDiscoveryFilterByName(String filterName, DSpaceObject scope) {
		List<DiscoverySearchFilter> filters = getDiscoveryConfiguration(scope).getSearchFilters();
		for (DiscoverySearchFilter discoverySearchFilter : filters) {
			if(discoverySearchFilter.getIndexFieldName().equals(filterName)) {
				return discoverySearchFilter;
			}
		}
		return null;
	}


	/**
	 * Conseguimos el label de año para una fecha dada. Por ejemplo, para la fecha '2018-01-10' se obtiene el valor 2018. 
	 * El formato de fecha aceptada es 'YYYY', 'YYYY-M', 'YYYY-M-dd' o el Datetime entero 'YYYY-M-dd'T'HH:mm:ss'.
	 */
	public static String getYearFromDate(String dateToParse) throws ParseException{
		if(isValidDate(dateToParse)) {
			DateTime dateTimeUtc = new DateTime( dateToParse, DateTimeZone.UTC );
			return String.valueOf(dateTimeUtc.getYear());
		}
		return null;
	}
	
	/**
	 * Conseguimos el label de año para una fecha dada. Por ejemplo, para la fecha "Wed Feb 18 08:03:58 ART 2015" se obtiene el valor 2015. 
	 * El formato de fecha aceptada es "EEE MMM dd HH:mm:ss Z yyyy".
	 */
	public static String getYearFromCompleteDate(String dateToParse) throws ParseException{
		//Para mas información ver https://stackoverflow.com/questions/11239814/parsing-a-java-date-back-from-tostring
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
		Date date = sdf.parse(dateToParse);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return String.valueOf(calendar.get(Calendar.YEAR));
	}
	
	/**
	 * Conseguimos el label de año-mes-dia para una fecha dada. Por ejemplo, para la fecha "2015-02-18T11:03:58Z" se obtiene el valor "2015-02-18". 
	 * El formato de fecha aceptada es "yyyy-MM-dd'T'HH:mm:ss'Z'".
	 */
	public static String getDateFromDatetime(String dateToParse) throws ParseException{
		//Para mas información ver https://stackoverflow.com/questions/11239814/parsing-a-java-date-back-from-tostring
		SimpleDateFormat datetimeformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = datetimeformat.parse(dateToParse);
		return dateformat.format(date);
	}
	
	/**
	 * Conseguimos la fecha en formato UTC para una fecha dada. El formato de fecha aceptada es "EEE MMM dd HH:mm:ss Z yyyy".
	 * Por ejemplo, para la fecha "Wed Feb 18 08:03:58 ART 2015" se obtiene el valor "2015-02-18T11:03:58.000Z". 
	 * 
	 */
	public static String getUTCFromCompleteDate(String dateToParse) throws ParseException{
		//Para mas información ver https://stackoverflow.com/questions/11239814/parsing-a-java-date-back-from-tostring
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
		Date date = sdf.parse(dateToParse);
		TimeZone utc = TimeZone.getTimeZone("UTC");
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.setTimeZone(utc);
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(utc);
		return formatter.format(calendar.getTime());
	}


	/**
	 * Conseguimos el label del datetime correspondiente a una fecha dada. Por ejemplo, para la fecha '2018-01-10' se obtiene el valor '2018-01-01T00:00:00.000Z'. 
	 * El formato de fecha aceptada es 'YYYY', 'YYYY-M', 'YYYY-M-dd' o el Datetime entero 'YYYY-M-dd'T'HH:mm:ss'.
	 */
	public static String getDateTimeStringFromDate(String dateToParse) throws ParseException{
		if(isValidDate(dateToParse)) {
			DateTime dateTimeUtc = getDateTimeFromDate(dateToParse);
			return dateTimeUtc.toString();
		}
		return null;
	}
	
	/**
	 * Conseguimos el objeto DateTime del datetime correspondiente a una fecha dada. Por ejemplo, para la fecha '2018-01-10' se obtiene el valor '2018-01-01T00:00:00.000Z'. 
	 * El formato de fecha aceptada es 'YYYY', 'YYYY-M', 'YYYY-M-dd' o el Datetime entero 'YYYY-M-dd'T'HH:mm:ss'.
	 */
	public static DateTime getDateTimeFromDate(String dateToParse) throws ParseException{
		if(isValidDate(dateToParse)) {
			return new DateTime( dateToParse, DateTimeZone.UTC );
		}
		return null;
	}


	/**
	 * Revisamos si la fecha pasada como parámetro es parseable al formato UTC. Por ejemplo, si se recibe la fecha en el formato 
	 * hora Argentina '2017-06-10T00:00:00-03:00' (ART), se verificará si se podrá pasar al formato universal '2017-06-10T03:00:00Z' (UTC).
	 * 
	 * @param dateRepresentation	es el String correspondiente a la fecha que se quiere parsear a UTC
	 * @return true si es parseable
	 */
	public static boolean isValidDate(String dateRepresentation) {
		try{
			new DateTime(dateRepresentation, DateTimeZone.UTC );
			return true;
		} catch (IllegalArgumentException e) {
			//Cuando se le pasa un String no válido al constructor de DateTime, entonces salta la IllegalArgumentException...
			return false;
		}
	}
	
	
	//TODO eliminamos este metodo en favor del escapeQueryChars???
	/**
	 * Realiza un escapado de los caracteres especiales en Solr, que son los siguientes: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /. 
	 * @see http://lucene.apache.org/core/6_5_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Escaping_Special_Characters
	 * @param target	el string a escapar los caraceteres especiales
	 * @return el parámetro pasado con un escapado de los caracteres especiales
	 */
	public static String escapeForSolrQuery(String target) {
		ArrayList<String> solrSpecialChars = new ArrayList<String>();
		solrSpecialChars.add("+"); solrSpecialChars.add("-"); solrSpecialChars.add("&"); solrSpecialChars.add("|"); solrSpecialChars.add("!");
		solrSpecialChars.add("("); solrSpecialChars.add(")"); solrSpecialChars.add("["); solrSpecialChars.add("]"); solrSpecialChars.add("^");
		solrSpecialChars.add("\""); solrSpecialChars.add("~"); solrSpecialChars.add("*"); solrSpecialChars.add("?"); solrSpecialChars.add(":");
		solrSpecialChars.add("\\"); solrSpecialChars.add("/");
		
		char[] chars = target.toCharArray();
		StringBuilder result = new StringBuilder();
		for (char ch : chars) {
			if(solrSpecialChars.contains(String.valueOf(ch))) {
				result.append("\\");
			}
			result.append(ch);
		}
		return result.toString();
	}
	
	/**
	 * Realiza un escapado de los caracteres especiales en Solr, que son los siguientes: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ / 
	 * @see http://lucene.apache.org/core/6_5_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Escaping_Special_Characters
	 * @param target	el string a escapar los caraceteres especiales
	 * @return el parámetro pasado con un escapado de los caracteres especiales
	 */
    public static String escapeQueryChars(String target) {
        // Use Solr's built in query escape tool
        // WARNING: You should only escape characters from user entered queries,
        // otherwise you may accidentally BREAK field-based queries (which often
        // rely on special characters to separate the field from the query value)
        return ClientUtils.escapeQueryChars(target);
        
        
    }
    
    /**
     * Se escapan los siguiente caracteres específicos de expresiones regulares: <([{\^-=$!|]})?*+."/~>.
     * Mas informacion: http://lucene.apache.org/core/4_10_1/core/org/apache/lucene/util/automaton/RegExp.html
     * @param target
     * @return el parámetro pasado con un escapado de los caracteres reservados para expresiones regulares
     */
    public static String escapeRegexReservedChars(String target){
  	  return target.replaceAll("[\\<\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)\\?\\*\\+\\.\\\"\\/\\~\\>]", "\\\\$0");
    }
    
}
