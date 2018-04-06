package org.dspace.app.xmlui.cocoon;

public class StatisticsDiscoveryJSONReportParams {

	private static String COUNTOF = "countof";
	private static String BY = "by";
	private static String TIMELAPSE = "timelapse";
	//Optional parameter
	public static String MINRESULTS = "minresults";
	
	
	/**
	 * <p>Lits of values and parameters accepted by this kind of only one variable reports.</p>
	 * <p>Lista de parámetros y valores aceptados para reportes de una sola variable.</p>
	 */
	public static class OneVarReports{
		/**	Lista de parámetros aceptados**/
		public static String COUNTOF_PARAM = StatisticsDiscoveryJSONReportParams.COUNTOF;
		/** Parametro opcional utilizado para indicar que se quiere un reporte por cantidad y fecha**/
		public static String TIMELAPSE_PARAM = StatisticsDiscoveryJSONReportParams.TIMELAPSE;

		//TODO se tendria que agregar un parámetro que permita "agrupar por cantidad", por ejemplo, agrupar IPs por cantidad de ocurrencias en los registros, es decir, hacer el arreglo que diga: [(10 IPs, con 100 ocurrencias en registros), (5 IPs, con 70 ocurrencias en registros, y así sucesivamente...)
		
		/**	Lista de valores aceptados por los parámetros **/
		public static enum COUNTOF_VALUES{
			CITY_FIELD("city"),	CONTINENT_FIELD("continent"), COUNTRY_FIELD("countryCode"), STATYSTICSTYPE_FIELD("statistics_type"),
			//DSO TYPE
			TYPE_FIELD("type"), IP_FIELD("ip");
			
			private char[] parameterName;
			private char[] solrField;
			//If no solr field is passed, then assume that is the same than the parameterName
			private COUNTOF_VALUES(String name) {
				this.parameterName = name.toCharArray();
				this.solrField = name.toCharArray();
			}
			private COUNTOF_VALUES(String name, String solrField) {
				this.parameterName = name.toCharArray();
				this.solrField = solrField.toCharArray();
			}
			public String toString() {
				return new String(parameterName);
			}
			
			public String getSolrFieldName() {
				return new String(solrField);
			}
			
			/**
			 * Verifica si un valor es válido dentro de los valores posibles de este enumerativo
			 * @param aValue	el valor a verificar
			 * @return	true si el parámetro es un valor válido dentro de los posibles valores de este enumerativo, false en caso contrario.
			 */
			public static boolean isValid(String aValue) {
				return getEnumByValue(aValue) != null;
			}
			
			/**
			 * Devuelve el enumerativo relativo al valor pasado como parémtro.
			 * @param aValue
			 * @return el enumerativo relativo al valor, o NULL en caso de que el valor no se corresponda con ningun enumerativo
			 */
			public static COUNTOF_VALUES getEnumByValue(String aValue) {
				if(aValue != null) {
					for (COUNTOF_VALUES countofValue : COUNTOF_VALUES.values()) {
						if(countofValue.toString().equalsIgnoreCase(aValue)) {
							return countofValue;
						}
					}
				}
				return null;
			}
		}
		
		public static enum TIMELAPSE_VALUES{
			MONTHLY("month","+1MONTH"), YEARLY("year","+1YEAR");
			
			private char[] parameterName;
			private char[] gap;
			private TIMELAPSE_VALUES(String name, String gap) {
				this.parameterName = name.toCharArray();
				this.gap= gap.toCharArray();
			}
			public String toString() {
				return new String(parameterName);
			}
			public String getSolrGap() {
				return new String(this.gap);
			}
			
			public static String getSolrField() {
				return "time";
			}
			
			/**
			 * Verifica si un valor es válido dentro de los valores posibles de este enumerativo
			 * @param aValue	el valor a verificar
			 * @return	true si el parámetro es un valor válido dentro de los posibles valores de este enumerativo, false en caso contrario.
			 */
			public static boolean isValid(String aValue) {
				return getEnumByValue(aValue) != null;
			}
			
			/**
			 * Devuelve el enumerativo relativo al valor pasado como parémtro.
			 * @param aValue
			 * @return el enumerativo relativo al valor, o NULL en caso de que el valor no se corresponda con ningun enumerativo
			 */
			public static TIMELAPSE_VALUES getEnumByValue(String aValue) {
				if(aValue != null) {
					for (TIMELAPSE_VALUES timelapseValue : TIMELAPSE_VALUES.values()) {
						if(timelapseValue.toString().equalsIgnoreCase(aValue)) {
							return timelapseValue;
						}
					}
				}
				return null;
			}
		}

		/**
		 * Verifica si los parámetros pasados son válidos para este tipo de reporte
		 * 
		 *	@param countOfParam 	Representa el valor a checkear para el parametro countof. Puede ser null
		 *	@param timelapseParam   Representa el valor a checkear para el parametro timelapse.Puede ser null
		 *
		 *	@return 'true' si todas las variables no nulas pasadas como parámetros son valores válidos, 'false' en caso de que 
		 *	alguna de las variables no sea válida o ambas sean nulas
		 */
		public static boolean checkParametersValues(String countOfParam, String timelapseParam) {
			if(countOfParam == null && timelapseParam == null) {
				return false;
			}
			if(countOfParam != null && !COUNTOF_VALUES.isValid(countOfParam)) { return false; }
			if(timelapseParam != null && !TIMELAPSE_VALUES.isValid(timelapseParam)) { return false; }
			
			//If no exit until now, then all parameters are valid!
			return true;
		}
	}
	
	/**
	 * <p>Lits of values and parameters accepted by this kind of two variables reports, where one of them is a fixed variable. For example, "statistics_type=view" is the fixed variable & "countryCode" is the not fixed variable.</p>
	 * 
	 * <p>Lista de parámetros y valores aceptados para reportes de dos variables, donde una de ellas es una variable fija. Por ejemplo, Por ejemplo, "statistics_type=view" es la variable fija y "countryCode" es la variable no fija.</p>
	 */
	public static class TwoVarsOneFixedReports{
		/**	Lista de parámetros aceptados**/
		/** variable principal **/
		public static String COUNTOF_PARAM = StatisticsDiscoveryJSONReportParams.COUNTOF;
		/** variable secundaria **/
		public static String BY_PARAM = StatisticsDiscoveryJSONReportParams.BY;
		/** Parametro opcional utilizado para indicar que se quiere un reporte por cantidad y fecha**/
		public static String TIMELAPSE_PARAM = StatisticsDiscoveryJSONReportParams.TIMELAPSE;
		
		/**	Lista de valores aceptados por los parámetros **/
		
		public static enum COUNTOF_VALUES{
			STATISTICSTYPE_VIEW_ITEM("item_view","statistics_type","view","statistics_type:view AND type:2"), 
			STATISTICSTYPE_VIEW_COLLECTION("collection_view","statistics_type","view","statistics_type:view AND type:3"), 
			STATISTICSTYPE_VIEW_COMMUNITY("community_view","statistics_type","view","statistics_type:view AND type:4"), 
			STATISTICSTYPE_DOWNLOAD("download","statistics_type","view","statistics_type:view AND type:0"), 
			STATISTICSTYPE_SEARCH("search","statistics_type","search"), 
			STATISTICSTYPE_SEARCH_COLLECTION("collection_search","statistics_type","search","statistics_type:search AND type:3"), 
			STATISTICSTYPE_SEARCH_COMMUNITY("community_search","statistics_type","search","statistics_type:search AND type:4"), 
			STATISTICSTYPE_WORKFLOW("workflow","statistics_type","workflow");
			//TYPE_COMMUNITY("community","type","4"), TYPE_COLLECTION("collection","type","3"), TYPE_ITEM("item","type","2"), 
			//TYPE_BITSTREAM("bitstream","type","0");
			
			private char[] parameterName;
			private char[] solrField;
			private char[] solrFilterValue;
			private char[] solrCustomFilterQuery;
			private COUNTOF_VALUES(String name, String solrField, String solrFilterValue, String solrCustomFilterValue) {
				this.parameterName = name.toCharArray();
				this.solrField = solrField.toCharArray();
				this.solrFilterValue = solrFilterValue.toCharArray();
				this.solrCustomFilterQuery = solrCustomFilterValue.toCharArray();
			}
			private COUNTOF_VALUES(String name, String solrField, String solrFilterValue) {
				this.parameterName = name.toCharArray();
				this.solrField = solrField.toCharArray();
				this.solrFilterValue = solrFilterValue.toCharArray();
				this.solrCustomFilterQuery = null;
			}
			public String toString() {
				return new String(parameterName);
			}
			
			public String getSolrFieldName() {
				return new String(solrField);
			}
			public String getSolrFilterValue() {
				return new String(solrFilterValue);
			}
			public String getFilterQuery() {
				if(solrCustomFilterQuery != null) {
					return new String(solrCustomFilterQuery);
				}
				return new String(solrField) + ":" + new String(solrFilterValue);
			}
			
			/**
			 * Verifica si un valor es válido dentro de los valores posibles de este enumerativo
			 * @param aValue	el valor a verificar
			 * @return	true si el parámetro es un valor válido dentro de los posibles valores de este enumerativo, false en caso contrario.
			 */
			public static boolean isValid(String aValue) {
				return getEnumByValue(aValue) != null;
			}
			
			/**
			 * Devuelve el enumerativo relativo al valor pasado como parémtro.
			 * @param aValue
			 * @return el enumerativo relativo al valor, o NULL en caso de que el valor no se corresponda con ningun enumerativo
			 */
			public static COUNTOF_VALUES getEnumByValue(String aValue) {
				if(aValue != null) {
					for (COUNTOF_VALUES countofValue : COUNTOF_VALUES.values()) {
						if(countofValue.toString().equalsIgnoreCase(aValue)) {
							return countofValue;
						}
					}
				}
				return null;
			}
		}
		
		public static enum BY_VALUES{
			CITY_FIELD("city"),	CONTINENT_FIELD("continent"), COUNTRY_FIELD("countryCode"), IP_FIELD("ip");
			
			private char[] parameterValue;
			private char[] solrField;
			//If no solr field is passed, then assume that is the same than the parameterName
			private BY_VALUES(String name) {
				this.parameterValue = name.toCharArray();
				this.solrField = name.toCharArray();
			}
			private BY_VALUES(String name, String solrField) {
				this.parameterValue = name.toCharArray();
				this.solrField = solrField.toCharArray();
			}
			public String toString() {
				return new String(parameterValue);
			}
			public String getSolrFieldName() {
				return new String(solrField);
			}
			
			/**
			 * Verifica si un valor es válido dentro de los valores posibles de este enumerativo
			 * @param aValue	el valor a verificar
			 * @return	true si el parámetro es un valor válido dentro de los posibles valores de este enumerativo, false en caso contrario.
			 */
			public static boolean isValid(String aValue) {
				return getEnumByValue(aValue) != null;
			}
			
			/**
			 * Devuelve el enumerativo relativo al valor pasado como parémtro.
			 * @param aValue
			 * @return el enumerativo relativo al valor, o NULL en caso de que el valor no se corresponda con ningun enumerativo
			 */
			public static BY_VALUES getEnumByValue(String aValue) {
				if(aValue != null) {
					for (BY_VALUES byValue : BY_VALUES.values()) {
						if(byValue.toString().equalsIgnoreCase(aValue)) {
							return byValue;
						}
					}
				}
				return null;
			}
		}
		
		public static enum TIMELAPSE_VALUES{
			MONTHLY("month","+1MONTH"), YEARLY("year","+1YEAR");
			
			private char[] parameterName;
			private char[] gap;
			private TIMELAPSE_VALUES(String name, String gap) {
				this.parameterName = name.toCharArray();
				this.gap= gap.toCharArray();
			}
			public String toString() {
				return new String(parameterName);
			}
			public String getSolrGap() {
				return new String(this.gap);
			}
			
			public static String getSolrField() {
				return "time";
			}
			
			/**
			 * Verifica si un valor es válido dentro de los valores posibles de este enumerativo
			 * @param aValue	el valor a verificar
			 * @return	true si el parámetro es un valor válido dentro de los posibles valores de este enumerativo, false en caso contrario.
			 */
			public static boolean isValid(String aValue) {
				return getEnumByValue(aValue) != null;
			}
			
			/**
			 * Devuelve el enumerativo relativo al valor pasado como parémtro.
			 * @param aValue
			 * @return el enumerativo relativo al valor, o NULL en caso de que el valor no se corresponda con ningun enumerativo
			 */
			public static TIMELAPSE_VALUES getEnumByValue(String aValue) {
				if(aValue != null) {
					for (TIMELAPSE_VALUES timelapseValue : TIMELAPSE_VALUES.values()) {
						if(timelapseValue.toString().equalsIgnoreCase(aValue)) {
							return timelapseValue;
						}
					}
				}
				return null;
			}
		}
		
		
		/**
		 * Verifica si los parámetros pasados son válidos para este tipo de reporte
		 * 
		 *	@param countOfParam 	Representa el valor a checkear para el parametro 'countof'. Puede ser null
		 *	@param byParam   		Representa el valor a checkear para el parametro 'by'.Puede ser null
		 *	@param timelapseParam   Representa el valor a checkear para el parametro 'timelapse'.Puede ser null
		 *
		 *	@return 'true' si todas las variables no nulas pasadas como parámetros son valores válidos, 'false' en caso de que 
		 *	alguna de las variables no sea válida o todas sean nulas.
		 */
		public static boolean checkParametersValues(String countOfParam, String byParam, String timelapseParam) {
			if(countOfParam == null && byParam == null && timelapseParam == null) {
				return false;
			}
			if(countOfParam != null && !COUNTOF_VALUES.isValid(countOfParam)) { return false; }
			if(byParam != null && !BY_VALUES.isValid(byParam)) { return false; }
			if(timelapseParam != null && !TIMELAPSE_VALUES.isValid(timelapseParam)) { return false; }
			
			//If no exit until now, then all parameters are valid!
			return true;
		}
		
	}
	
	
}
