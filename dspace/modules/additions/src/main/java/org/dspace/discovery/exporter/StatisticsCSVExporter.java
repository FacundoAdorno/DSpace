package org.dspace.discovery.exporter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult;
import org.dspace.discovery.exporter.csv.StatisticsDSpaceCSV;

public class StatisticsCSVExporter extends StatisticsExportStrategy {

	@Override
	public BufferedInputStream export(StatisticsDiscoverResult result, Charset defaultEncoding) {
		if(result != null && result.getAllResults().size() > 1) {
			StatisticsDSpaceCSV csv = new StatisticsDSpaceCSV();
			for (SearchDocument document : result.getAllResults()) {
				csv.addSearchDocument(document);
			}
			return new BufferedInputStream(new ByteArrayInputStream(csv.toString().getBytes(defaultEncoding)));
		} else {
			//Si es null, entonces retornamos un texto vacío...
			return new BufferedInputStream(new ByteArrayInputStream("".getBytes(defaultEncoding)));
		}
	}
	
	@Override
	public String getFormatExtension() {
		return "csv";
	}

	@Override
	public String getMimetype() {
		return "text/csv";
	}

	@Override
	public void setupExporter(Map<String, String> requestParameters) {
		//Por defecto, no hace nada... Por ahora, la exportación a CSV no tiene ninguna configuración disponible
	}

}
