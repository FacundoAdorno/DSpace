package org.dspace.discovery.exporter;

import java.util.ArrayList;
import java.util.Arrays;

import org.dspace.core.Constants;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;

public class StatisticsCommonResponseTransformer implements StatisticsResponseTransformer {

	protected static String DSO_TYPE_FIELD = "type";
	protected static String SCOPE_TYPE_FIELD = "scopeType";
	
	@Override
	public void beforeQuery(DiscoverQuery query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterQuery(SearchDocument document) {
		ArrayList<String> newValues;
		
		for (String dsoField : new String[] {DSO_TYPE_FIELD, SCOPE_TYPE_FIELD}) {
			if(!document.getSearchFieldValues((dsoField)).isEmpty()) {
				String dsoFieldValue = document.getSearchFieldValues((dsoField)).get(0);
				int dsoType = Integer.parseInt(dsoFieldValue);
				ArrayList<String> constants = new ArrayList<String>(Arrays.asList(Constants.typeText));
				//chequeamos si el DSO_TYPE_FIELD está dentro de los valores de constantes por las dudas
				if(constants.size() >= dsoType) {
					newValues = new ArrayList<String>();
					newValues.add(constants.get(dsoType));
					document.replaceFieldValues(dsoField, newValues);
				}
			}
		}
	}

}
