package org.dspace.discovery.exporter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class StatisticsJSONExporter extends StatisticsExportStrategy {

	public static enum JsonExporterParams {
		PRETTY_FORMAT("json.format");
		
		private char[] parameterName;
		private JsonExporterParams(String name) {
			this.parameterName = name.toCharArray();
		}
		public String toString() {
			return new String(parameterName);
		}
	}
	
	/**
	 * Determina si la exportación JSON debe realizarse de forma formateada o no. (Ver <a href="https://fasterxml.github.io/jackson-core/javadoc/2.2.0/com/fasterxml/jackson/core/PrettyPrinter.html">PrettyPrinter<> para mas información)
	 */
	private boolean prettyFormat = false;
	
	private JsonGenerator generator;
	
	@Override
	public BufferedInputStream export(StatisticsDiscoverResult result, Charset defaultEncoding) throws UnsupportedEncodingException, StatisticsDiscoveryExportException {
		
		JsonEncoding encoding = getJsonEncoding(defaultEncoding);
		//Si el encoding no es soportado por el exportador, entonces tiramos una excepción...
		if(encoding == null) {
			throw new UnsupportedEncodingException();
		}
		JsonFactory factory = new JsonFactory();
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			generator = factory.createGenerator(output, encoding);
			//Si se pasó el parámetro para formatearlo, entonces usamos el PrettyFormat por default. Sino, entonces no se realiza ningun formateo de la exportación.
			if(this.prettyFormat) {
				generator.useDefaultPrettyPrinter();
			}
			// Comenzamos a escribir la respuesta JSON con "{"
			generator.writeStartObject();
			generator.writeNumberField("size", result.getTotalSearchResults());
			generator.writeFieldName("statistics_records");
			generator.writeStartArray();
			for (SearchDocument document : result.getAllResults()) {
				generator.writeStartObject();
				Map<String, List<String>> fields = document.getSearchFields();
				for ( String field : fields.keySet()) {
					List<String> values = fields.get(field);
					if(!values.isEmpty()) {
						if(values.size() == 1) {
							//Si hay un solo valor para imprimir...
							generateSimpleStringField(field, values.get(0));
						} else {
							//Sino hay varios valores que imprimir
							generateArrayStringFields(field, values);
						}
					}
				}
				//Escribimos el final del campo "statistics_records"
				generator.writeEndObject();
			}
			generator.writeEndArray();
			//Escribimos el final de la respuesta
			generator.writeEndObject();
			
			generator.close();
			
			return new BufferedInputStream( new ByteArrayInputStream(output.toByteArray()));
			
		} catch (IOException e) {
			throw new StatisticsDiscoveryExportException(e);
		}
		
	}

	@Override
	public String getFormatExtension() {
		return "json";
	}

	@Override
	public String getMimetype() {
		return "application/json";
	}
	
	public void setupExporter(Map<String,String> requestParameters) {
		for (String paramName : requestParameters.keySet()) {
			if(paramName.equals(JsonExporterParams.PRETTY_FORMAT.toString())) {
				this.prettyFormat = true;
			}
		}
	}
	
	
	/**
	 * @param fromCharset 	el encoding del que nos interesa obtener el JsonEncoding corespondiente.
	 * @return Retorna el JsonEncoding correspondiente para el Charset pasado como parámetro, o @null si el Charset no tiene correspondencia en JsonEncoding.
	 */
	private JsonEncoding getJsonEncoding(Charset fromCharset) {
		//Obtenemos el nombre canónico del plugin mas los aliases para ver si se puede obtener un JsonEncoder correspondiente a este Charset...
		ArrayList<String> nameAndAliases = new ArrayList<String>();
		nameAndAliases.add(fromCharset.name());
		nameAndAliases.addAll(fromCharset.aliases());
		for (String charsetNameVariant : nameAndAliases) {
			try {
				if(JsonEncoding.valueOf(charsetNameVariant)!= null) {
					return JsonEncoding.valueOf(charsetNameVariant);
				}
			} catch (IllegalArgumentException e) {
				//Cada vez que llamamos al JsonEncoding#valueOf con un valor que no conoce, entonces salta esta excepción
				//No hacemos nada porque nos interesa saber si el Charset tiene un JsonEncoding correspondiente a su nombre o alguno de sus aliases, asi que seguimos buscando
			}
		}
		return null;
	}
	
	
	private void generateSimpleStringField(String fieldName, String value) throws IOException{
		generator.writeStringField(fieldName, value);
	}
	
	private void generateArrayStringFields(String fieldName, List<String> values) throws IOException{
		generator.writeFieldName(fieldName);
		generator.writeStartArray();
		for (String value : values) {
			generator.writeString(value);
		}
		generator.writeEndArray();
	}
}
