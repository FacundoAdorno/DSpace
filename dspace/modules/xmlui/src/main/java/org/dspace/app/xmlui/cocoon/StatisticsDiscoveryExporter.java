package org.dspace.app.xmlui.cocoon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.discovery.StatisticsSimpleSearch;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult;
import org.dspace.discovery.StatisticsSearchServiceException;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.exporter.StatisticsExportStrategy;
import org.dspace.discovery.exporter.StatisticsExportStrategy.StatisticsDiscoveryExportException;
import org.dspace.discovery.exporter.StatisticsResponseTransformer;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;


public class StatisticsDiscoveryExporter extends AbstractReader {

	/**
     * How big a buffer should we use when reading from the bitstream before
     * writing to the HTTP response?
     */
    protected static final int BUFFER_SIZE = 8192;
    
    /**
     * El Encoding por defecto utilizado en la exportación.
     */
    protected static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

    /**
     * When should a download expire in milliseconds. This should be set to
     * some low value just to prevent someone hitting DSpace repeatedly from
     * killing the server. Note: there are 60000 milliseconds in a minute.
     * 
     * Format: minutes * seconds * milliseconds
     */
    protected static final int expires = 60 * 60 * 60000;
	
	/** The Cocoon response */
    protected Response response;

    /** The Cocoon request */
    protected Request request;
    
    
    private static Logger log = Logger.getLogger(StatisticsDiscoveryExporter.class);
    
    private BufferedInputStream toExportStream = null;
    
    private String filename = null;
    
    private StatisticsSimpleSearch statisticsSimpleSearch = null;
	
    private StatisticsExportStrategy exportStrategy = null;
    
    private StatisticsDiscoverResult qResults = null;
    
    private List<StatisticsResponseTransformer> responseTransformers = new ArrayList<StatisticsResponseTransformer>();
    
    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    		throws ProcessingException, SAXException, IOException {
    	super.setup(resolver, objectModel, src, par);
    	
    	this.request = ObjectModelHelper.getRequest(objectModel);
    	this.response = ObjectModelHelper.getResponse(objectModel);
    	statisticsSimpleSearch = new StatisticsSimpleSearch();
//    	toExportStream = exportMetadata(context, objectModel, query, scope, filters);
    	
    	
    	//Configuramos los exporters disponibles desde la configuración...
    	HashMap<String,StatisticsExportStrategy> exportStrategies = getAvailablesExportStrategies();
    	
    	//Configuramos los exporters disponibles desde la configuración...
    	String[] responseTransformersList = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("statistics-discovery.exporter.response-transformers");
    	for (String responseTransformerClass : responseTransformersList) {
    		try {
    			StatisticsResponseTransformer transformer = (StatisticsResponseTransformer)getInstanceFromClassProperty(responseTransformerClass);
    			responseTransformers.add(transformer);
    		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    			log.error("La clase correspondiente al responseTransformer '" + responseTransformerClass +"' no puede inicializarse correctamente!!!", e);
    		}
    	}
    	
    	String exportStrategySelector = parameters.getParameter("exporterSelector",null);
    	if(exportStrategySelector!=null || exportStrategies.containsKey(exportStrategySelector)) {
    		exportStrategy = exportStrategies.get(exportStrategySelector);
    	} else {
    		throw new ProcessingException("No existe ninguna exportacion disponible para la ruta especificada!!!");
    	}
    	
        filename = exportStrategy.getFilenameFor("statistics-discovery-resuls");
        
        try {
			performSearch();
		} catch (UIException | StatisticsSearchServiceException | SQLException e) {
			throw new ProcessingException(e);
		}
        
        try {
			toExportStream = exportStrategy.export(qResults, DEFAULT_ENCODING);
		} catch ( UnsupportedEncodingException  e) {
			//TODO habría que implementar un mecanismo para que los exportadores puedan realizar la exportación utilizando algun Charset que ellos sepan que puedan procesar sin tener que cortar la ejecución...
			throw new ProcessingException("El encoding '" + DEFAULT_ENCODING.displayName() + "' no es soportado por el exportador " + exportStrategy.getClass(), e);
		} catch (StatisticsDiscoveryExportException e) {
			throw new ProcessingException("Se ha producido un error durante el proceso de exportación", e);
		}
    }


	/**
	 * @return 	una lista de opciones disponibles para exportación de resultados de statistics
	 */
	public static List<String> getAvailablesExportOptions() {
		return DSpaceServicesFactory.getInstance().getConfigurationService().getPropertyKeys("statistics-discovery.exporter.selector.");
	}
	
	public static HashMap<String,StatisticsExportStrategy> getAvailablesExportStrategies(){
		HashMap<String,StatisticsExportStrategy> exportStrategies = new HashMap<String,StatisticsExportStrategy>();
		for (String exporterSelector : getAvailablesExportOptions()) {
    		String exporterSelectorClass = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(exporterSelector);
    		try {
    			StatisticsExportStrategy exporter = (StatisticsExportStrategy)getInstanceFromClassProperty(exporterSelectorClass);
    			exportStrategies.put(exporterSelector.replace("statistics-discovery.exporter.selector.", ""), exporter);
    		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    			log.error("La clase correspondiente al exportador '" + exporterSelector +"' no puede inicializarse correctamente!!!", e);
			}
		}
		return exportStrategies;
	}


	/**
	 * @param classFromProperty
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static Object getInstanceFromClassProperty(String classFromProperty)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		if(classFromProperty == null) {
			throw new ClassNotFoundException("Se ha pasado un clase vacia como argumento...");
		}
		Class<?> clazz = Class.forName(classFromProperty);
		Constructor<?> ctor = clazz.getConstructor();
//    		Object object = ctor.newInstance(new Object[] { ctorArgument });
		return ctor.newInstance();
	}
    

	@Override
	public void generate() throws IOException, SAXException, ProcessingException {
		if(toExportStream != null){
            byte[] buffer = new byte[BUFFER_SIZE];

            response.setContentType(exportStrategy.getMimetype() + "; charset=" + DEFAULT_ENCODING.displayName());
            response.setHeader("Content-Disposition","attachment; filename=" + filename);
            response.setHeader("Content-Length", String.valueOf(toExportStream.available()));
            int length;
            while ((length = toExportStream.read(buffer)) > -1)
            {
                out.write(buffer, 0, length);
            }
        }
        out.flush();

	}

	private void performSearch() throws StatisticsSearchServiceException, SQLException, UIException {
		DiscoverQuery qArgs;
		
		Context context = ContextUtil.obtainContext(objectModel);
		
		Context.Mode originalMode = context.getCurrentMode();
        context.setMode(Context.Mode.READ_ONLY);

        Request request = ObjectModelHelper.getRequest(objectModel);
        
     // set the object model on the simple search object
        statisticsSimpleSearch.objectModel = objectModel;
        String queryString = statisticsSimpleSearch.getQuery();
        DSpaceObject scope = statisticsSimpleSearch.getScope();
        String[] fqs = statisticsSimpleSearch.getFilterQueries();
        
     // prepare query from SatisticsSimpleSearch object
        qArgs = statisticsSimpleSearch.prepareQuery(scope, queryString, fqs);
        
        for (StatisticsResponseTransformer responseTransformer : responseTransformers) {
        	responseTransformer.beforeQuery(qArgs);
		}
        
     // no paging required
        qArgs.setStart(0);
                
        // some arbitrary value for first search
        qArgs.setMaxResults(10);
                
        // search once to get total search results
        qResults = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, qArgs);
                	        	
        // set max results to total search results
        qArgs.setMaxResults(safeLongToInt(qResults.getTotalSearchResults()));        	        	
        
        // search again to return all search results
        qResults = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, qArgs);
        
        for (SearchDocument document : qResults.getAllResults()) {
        	for (StatisticsResponseTransformer responseTransformer : responseTransformers) {
        		responseTransformer.afterQuery(document);
        	}
		}
	}
	
	private int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int.");
        }
        return (int) l;
    }
	
	/**
	 * Recycle
	 */
    @Override
    public void recycle() {        
        this.response = null;
        this.request = null;
        this.toExportStream = null;
        this.filename = null;
        this.statisticsSimpleSearch = null;
    	this.exportStrategy = null;
        this.qResults = null;
        this.responseTransformers = new ArrayList<StatisticsResponseTransformer>();
    }
}
