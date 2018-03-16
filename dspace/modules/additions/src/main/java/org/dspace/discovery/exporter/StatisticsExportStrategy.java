package org.dspace.discovery.exporter;

import java.io.BufferedInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.dspace.discovery.StatisticsDiscoverResult;

public abstract class StatisticsExportStrategy {

	/**
	 * Se encarga de realizar la exportación al formato de exportación que esta estrategia maneja.
	 * El encoding del InputStream resultante (p.e. "UTF-8") debe ser el mismo que el pasado como parametro.
	 * 
	 * @param result	son los resultados de una búsqueda en el core de 'statistics'
	 * @param preferredEncoding	el encoding deseado para el InputStream resultante
	 *
	 * @throws UnsupportedEncodingException si el preferredEncoding no es soportado por la estrategia de exportación.
	 * @throws StatisticsDiscoveryExportException cuando sucede cualquier otra situación genérica dentro durante la exportación.
	 * 
	 * <div>
	 * <b>TODO:</b> se debería poder aceptar el 'preferredEncoding' como null, e implementar un método "defaultEncodingCharset()" que 
	 * retorne el encoding que por defecto utiliza el exportador cuando no se le pasa el 'preferredEncoding' o éste último no pueda ser utilizado por el exportador
	 * </div>
	 */
	public abstract BufferedInputStream export(StatisticsDiscoverResult result, Charset preferredEncoding) throws UnsupportedEncodingException, StatisticsDiscoveryExportException;
	
	/**
	 * Retorna el tipo de extensión utilizada para los archivos de este tipo de exportación. Por ejemplo, si la exportación es XML entonces debería retornar "xml".
	 * @return
	 */
	public abstract String getFormatExtension();
	
	/**
	 * Retorna el nombre de archivo concatenado con la extensión relativa al exportador a partir de un nombre pasado como parámetro.
	 * @param name
	 * @return
	 */
	public String getFilenameFor(String name) {
		if(name==null || (name!= null && name.isEmpty())) {
			
		}
		return name + "." + getFormatExtension();
	}
	
	/**
	 * Método para saber que mimetype se corresponde con el formato de exportación.
	 * @return	un identificador MIME-Type correspondiente al formato exportado (p.e. "text/csv").
	 */
	public abstract String getMimetype();
	
	/**
	 * Se pasan los parámetros relativos a la solicitud de la exportación al exportador para que, a partir de éstos, éste pueda configurarse.
	 * El exportador puede tranquilamente no tener que configurarse a sí mismo, por lo que tendría que implementar este metodo como un método vacío...
	 * @param requestParameters		son un conjunto de parámetros relativos a la petición de exportación
	 */
	public abstract void setupExporter(Map<String,String> requestParameters);
	
	
	/**
	 * Excepción creada para informar de excepciones generadas en caso de que se produzca un error genérico durante la exportación
	 * @author facundo
	 */
	@SuppressWarnings("serial")
	public class StatisticsDiscoveryExportException extends Exception{
			
		public StatisticsDiscoveryExportException () {

	    }

	    public StatisticsDiscoveryExportException (String message) {
	        super (message);
	    }

	    public StatisticsDiscoveryExportException (Throwable cause) {
	        super (cause);
	    }

	    public StatisticsDiscoveryExportException (String message, Throwable cause) {
	        super (message, cause);
	    }
		
	}
	
}
