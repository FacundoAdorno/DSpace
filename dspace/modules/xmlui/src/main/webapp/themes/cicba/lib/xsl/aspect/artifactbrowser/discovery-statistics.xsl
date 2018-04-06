<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<xsl:stylesheet
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:encoder="xalan://java.net.URLEncoder"
    xmlns:xmlui="xalan://ar.edu.unlp.sedici.dspace.xmlui.util.XSLTHelper"
    extension-element-prefixes="xmlui"
    exclude-result-prefixes="xalan encoder i18n dri mets dim  xlink xsl">

    <xsl:output indent="yes"/>

<!--
    These templates are devoted to rendering the search results for Discovery.
    Since Discovery uses hit highlighting separate templates are required !
-->


    <xsl:template match="dri:list[@n='statistics-search-results-repository']" priority="2">
        <xsl:apply-templates select="dri:head"/>
        <ul class="statistics-discovery-list-results container-fluid">
            <xsl:apply-templates select="*[not(name()='head')]" mode="statisticsResultsList"/>
        </ul>
    </xsl:template>


    <xsl:template match="dri:list/dri:list" mode="statisticsResultsList" priority="7">
            <xsl:apply-templates select="*[not(name()='head')]" mode="statisticsResultsList"/>
    </xsl:template>


    <xsl:template match="dri:list/dri:list/dri:list" mode="statisticsResultsList" priority="8">
        <li class="row artifact-info" >
        	<xsl:variable name="type">
                <xsl:value-of select="substring-after(@n, ':statistics:')"/>
            </xsl:variable>
            <xsl:choose>
            	<xsl:when test="$type='view'">
            		<xsl:call-template name="viewStatisticsType"/>
            	</xsl:when>
            	<xsl:when test="$type='search'">
            		<xsl:call-template name="searchStatisticsType"/>
            	</xsl:when>
            	<xsl:when test="$type='workflow'">
            		<xsl:call-template name="workflowStatisticsType"/>
            	</xsl:when>
            </xsl:choose>
        </li>
    </xsl:template>
    
    <xsl:template name="viewStatisticsType">
    	<!-- El tipo de DSO accedido -->
        	<xsl:variable name="dsoType">
        		<xsl:choose>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='0'">
        				<xsl:value-of select="'BITSTREAM'"/>
        			</xsl:when>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='1'">
        				<xsl:value-of select="'BUNDLE'"/>
        			</xsl:when>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='2'">
        				<xsl:value-of select="'ITEM'"/>
        			</xsl:when>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='3'">
        				<xsl:value-of select="'COLLECTION'"/>
        			</xsl:when>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='4'">
        				<xsl:value-of select="'COMMUNITY'"/>
        			</xsl:when>
        			<xsl:otherwise><xsl:value-of select="concat('DSO=',./dri:list[@n='type']/dri:item[1]/text())"/></xsl:otherwise>
        		</xsl:choose>
        	</xsl:variable>
        	<!-- El ID del DSO accedido -->
        	<xsl:variable name="dsoId"><xsl:value-of select="dri:list[@n='id']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="time"><xsl:value-of select="dri:list[@n='time']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="ip"><xsl:value-of select="dri:list[@n='ip']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="city"><xsl:value-of select="dri:list[@n='city']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="country"><xsl:value-of select="dri:list[@n='countryCode']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="handle"><xsl:value-of select="dri:list[@n='handle']/dri:item[1]/text()"/></xsl:variable>
            <!-- primera fila -->
            <div class="col-md-3">
            	<span>
            	<xsl:choose>
	            		<xsl:when test="string-length($handle)>0">
	            			<xsl:call-template name="build-anchor">
	            				<xsl:with-param name="a.href"><xsl:value-of select="concat('handle/',$handle)"/></xsl:with-param>
	            				<xsl:with-param name="a.value"><xsl:value-of select="$dsoType"/> - ID:<xsl:value-of select="$dsoId"/></xsl:with-param>
	            			</xsl:call-template>
	            		</xsl:when>
	            		<xsl:otherwise>        		
			            	<xsl:value-of select="$dsoType"/> - ID:<xsl:value-of select="$dsoId"/>
	            		</xsl:otherwise>
            	</xsl:choose>
            	</span>
            </div>
            <div class="col-md-6">
            	<span>Tiempo de acceso: <xsl:value-of select="$time"/> </span>
            </div>
            <div class="col-md-3">
            	<span>VIEW</span>
            </div>
            <!-- segunda fila -->
            <div class="col-md-10">
            	<span>IP de acceso:  <xsl:value-of select="$ip"/> (<xsl:value-of select="$city"/>, <xsl:value-of select="$country"/>)</span>
            </div>
            <div class="col-md-2" title="Ver registro completo">
            	<xsl:call-template name="putPopUpForCompleteStatistic">
	            	<xsl:with-param name="context" select="."/>
	            </xsl:call-template>
            </div>
    </xsl:template>
    
    <!-- TODO falta terminar este template !!!!!!!!!! -->
    <xsl:template name="searchStatisticsType">
       	<!-- El ID y el tipo de DSO desde donde se realizó la busqueda -->
       	<xsl:variable name="scopeType">
       		<xsl:choose>
       			<xsl:when test="dri:list[@n='scopeType']/dri:item[1]/text()='3'">
       				<xsl:value-of select="'COLLECTION'"/>
       			</xsl:when>
       			<xsl:when test="dri:list[@n='scopeType']/dri:item[1]/text()='4'">
       				<xsl:value-of select="'COMMUNITY'"/>
       			</xsl:when>
       		</xsl:choose>
       	</xsl:variable>
       	<xsl:variable name="scopeId"><xsl:value-of select="dri:list[@n='scopeId']/dri:item[1]/text()"/></xsl:variable>
       	<!-- pueden haber varios términos de busqueda -->
       	<xsl:variable name="querySearchStrings"><xsl:value-of select="dri:list[@n='query']/dri:item"/></xsl:variable>
       	<xsl:variable name="searchInGlobalScope"><xsl:value-of select="$scopeId='' and $scopeType=''"/></xsl:variable>
       	<xsl:variable name="time"><xsl:value-of select="dri:list[@n='time']/dri:item[1]/text()"/></xsl:variable>
       	<xsl:variable name="ip"><xsl:value-of select="dri:list[@n='ip']/dri:item[1]/text()"/></xsl:variable>
       	<xsl:variable name="city"><xsl:value-of select="dri:list[@n='city']/dri:item[1]/text()"/></xsl:variable>
       	<xsl:variable name="country"><xsl:value-of select="dri:list[@n='countryCode']/dri:item[1]/text()"/></xsl:variable>
       	<xsl:variable name="handle"><xsl:value-of select="dri:list[@n='handle']/dri:item[1]/text()"/></xsl:variable>
           <!-- primera fila -->
           <div class="search-result first-row col-md-12">
	           <div class="col-md-3">
		           	<span>
	            	<xsl:choose>
		            		<xsl:when test="string-length($handle)>0">
		            			<xsl:call-template name="build-anchor">
		            				<xsl:with-param name="a.href"><xsl:value-of select="concat('handle/',$handle)"/></xsl:with-param>
		            				<xsl:with-param name="a.value"><xsl:value-of select="$scopeType"/> - ID:<xsl:value-of select="$scopeId"/></xsl:with-param>
		            			</xsl:call-template>
		            		</xsl:when>
		            		<xsl:when test="$searchInGlobalScope">
		            			BÚSQUEDA GENERAL
		            		</xsl:when>
		            		<xsl:otherwise>        		
				            	<xsl:value-of select="$scopeType"/> - ID:<xsl:value-of select="$scopeId"/>
		            		</xsl:otherwise>
	            	</xsl:choose>
	            	</span>
	           </div>
	           <div class="col-md-6">
	           	<span>Tiempo de acceso: <xsl:value-of select="$time"/> </span>
	           </div>
	           <div class="col-md-3">
	           	<span>SEARCH</span>
	           </div>
	       </div>
           <!-- segunda fila -->
           <div class="search-result second-row col-md-12">
	           <div class="col-md-10">
	           		<span>IP de acceso:  <xsl:value-of select="$ip"/> (<xsl:value-of select="$city"/>, <xsl:value-of select="$country"/>)</span>
	           </div>
	           <div class="col-md-2" title="Ver registro completo">
	            	<xsl:call-template name="putPopUpForCompleteStatistic">
		            	<xsl:with-param name="context" select="."/>
		            </xsl:call-template>
	            </div>
	       </div>
           <!-- tercera fila (TERMINOS DE BUSQUEDA) -->
           <!-- TODO implementar -->
    </xsl:template>
    
        <xsl:template name="workflowStatisticsType">
    	<!-- El tipo de DSO accedido / Hasta ahora solo existen de Items... -->
        	<xsl:variable name="dsoType">
        		<xsl:choose>
        			<xsl:when test="dri:list[@n='type']/dri:item[1]/text()='2'">
        				<xsl:value-of select="'ITEM'"/>
        			</xsl:when>
        			<xsl:otherwise><xsl:value-of select="concat('DSO=',./dri:list[@n='type']/dri:item[1]/text())"/></xsl:otherwise>
        		</xsl:choose>
        	</xsl:variable>
        	<!-- El ID del DSO accedido -->
        	<xsl:variable name="dsoId"><xsl:value-of select="dri:list[@n='id']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="time"><xsl:value-of select="dri:list[@n='time']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="ip"><xsl:value-of select="dri:list[@n='ip']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="city"><xsl:value-of select="dri:list[@n='city']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="country"><xsl:value-of select="dri:list[@n='countryCode']/dri:item[1]/text()"/></xsl:variable>
        	<xsl:variable name="handle"><xsl:value-of select="dri:list[@n='handle']/dri:item[1]/text()"/></xsl:variable>
            <!-- primera fila -->
            <div class="col-md-3">
            	<span>
            	<xsl:choose>
	            		<xsl:when test="string-length($handle)>0">
	            			<xsl:call-template name="build-anchor">
	            				<xsl:with-param name="a.href"><xsl:value-of select="concat('handle/',$handle)"/></xsl:with-param>
	            				<xsl:with-param name="a.value"><xsl:value-of select="$dsoType"/> - ID:<xsl:value-of select="$dsoId"/></xsl:with-param>
	            			</xsl:call-template>
	            		</xsl:when>
	            		<xsl:otherwise>        		
			            	<xsl:value-of select="$dsoType"/> - ID:<xsl:value-of select="$dsoId"/>
	            		</xsl:otherwise>
            	</xsl:choose>
            	</span>
            </div>
            <div class="col-md-6">
            	<span>Tiempo de acceso: <xsl:value-of select="$time"/> </span>
            </div>
            <div class="col-md-3">
            	<span>WORKFLOW</span>
            </div>
            <!-- segunda fila -->
            <div class="col-md-10">
            <span>IP de acceso:  <xsl:value-of select="$ip"/> (<xsl:value-of select="$city"/>, <xsl:value-of select="$country"/>)</span>
            </div>
            <div class="col-md-2" title="Ver registro completo">
            	<xsl:call-template name="putPopUpForCompleteStatistic">
            	<xsl:with-param name="context" select="."/>
            </xsl:call-template>
            </div>
    </xsl:template>
    
    <xsl:template name="putPopUpForCompleteStatistic">
    	<xsl:param name="context"/>
    	<!--  Guardamos todas los posibles campos para todos los tipos de estadisticas y mostrarlos en el popup-->
    	<!-- <xsl:variable name="version"><xsl:value-of select="$context/dri:list[@n='_version_']/dri:item[1]/text()"/></xsl:variable> -->
		<xsl:variable name="type"><xsl:value-of select="$context/dri:list[@n='type']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="id"><xsl:value-of select="$context/dri:list[@n='id']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="ip"><xsl:value-of select="$context/dri:list[@n='ip']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="time"><xsl:value-of select="$context/dri:list[@n='time']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="epersonid"><xsl:value-of select="$context/dri:list[@n='epersonid']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="continent"><xsl:value-of select="$context/dri:list[@n='continent']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="country"><xsl:value-of select="$context/dri:list[@n='country']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="countryCode"><xsl:value-of select="$context/dri:list[@n='countryCode']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="city"><xsl:value-of select="$context/dri:list[@n='city']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="longitude"><xsl:value-of select="$context/dri:list[@n='longitude']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="latitude"><xsl:value-of select="$context/dri:list[@n='latitude']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="owningComm"><xsl:value-of select="$context/dri:list[@n='owningComm']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="owningColl"><xsl:value-of select="$context/dri:list[@n='owningColl']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="owningItem"><xsl:value-of select="$context/dri:list[@n='owningItem']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="dns"><xsl:value-of select="$context/dri:list[@n='dns']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="userAgent"><xsl:value-of select="$context/dri:list[@n='userAgent']/dri:item[1]/text()"/></xsl:variable>
		<!-- <xsl:variable name="isBot"><xsl:value-of select="$context/dri:list[@n='isBot']/dri:item[1]/text()"/></xsl:variable> -->
		<xsl:variable name="bundleName"><xsl:value-of select="$context/dri:list[@n='bundleName']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="referrer"><xsl:value-of select="$context/dri:list[@n='referrer']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="uid"><xsl:value-of select="$context/dri:list[@n='uid']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="statistics_type"><xsl:value-of select="$context/dri:list[@n='statistics_type']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="query"><xsl:value-of select="$context/dri:list[@n='query']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="scopeType"><xsl:value-of select="$context/dri:list[@n='scopeType']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="scopeId"><xsl:value-of select="$context/dri:list[@n='scopeId']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="rpp"><xsl:value-of select="$context/dri:list[@n='rpp']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="sortBy"><xsl:value-of select="$context/dri:list[@n='sortBy']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="sortOrder"><xsl:value-of select="$context/dri:list[@n='sortOrder']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="page"><xsl:value-of select="$context/dri:list[@n='page']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="workflowStep"><xsl:value-of select="$context/dri:list[@n='workflowStep']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="previousWorkflowStep"><xsl:value-of select="$context/dri:list[@n='previousWorkflowStep']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="owner"><xsl:value-of select="$context/dri:list[@n='owner']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="submitter"><xsl:value-of select="$context/dri:list[@n='submitter']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="actor"><xsl:value-of select="$context/dri:list[@n='actor']/dri:item[1]/text()"/></xsl:variable>
		<xsl:variable name="workflowItemId"><xsl:value-of select="$context/dri:list[@n='workflowItemId']/dri:item[1]/text()"/></xsl:variable>
    	
    	
    	<xsl:variable name="popupid"><xsl:value-of select="concat('popup-for-',$uid)"/></xsl:variable>
    	<span class="glyphicon glyphicon-eye-open popup">
    		<xsl:attribute name="onclick">
    			<xsl:text>showPopUp('</xsl:text><xsl:value-of select="$popupid"/><xsl:text>');</xsl:text>
    		</xsl:attribute>
    		<div class="popuptext">
    			<xsl:attribute name="id"><xsl:value-of select="$popupid"/></xsl:attribute>
    			<ul>
    				<!-- <xsl:if test="$version">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.version</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$version"></xsl:value-of></span>
	    				</li>
    				</xsl:if> -->
					<xsl:if test="$type">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.type</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$type"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$id">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.id</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$id"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$ip">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.ip</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$ip"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$time">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.time</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$time"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$epersonid">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.epersonid</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$epersonid"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$continent">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.continent</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$continent"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$country">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.country</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$country"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$countryCode">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.countryCode</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$countryCode"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$city">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.city</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$city"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$longitude">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.longitude</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$longitude"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$latitude">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.latitude</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$latitude"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$owningComm">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.owningComm</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$owningComm"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$owningColl">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.owningColl</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$owningColl"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$owningItem">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.owningItem</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$owningItem"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$dns">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.dns</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$dns"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$userAgent">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.userAgent</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$userAgent"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<!-- <xsl:if test="$isBot">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.isBot</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$isBot"></xsl:value-of></span>
	    				</li>
    				</xsl:if> -->
					<xsl:if test="$bundleName">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.bundleName</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$bundleName"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$referrer">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.referrer</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$referrer"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$uid">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.uid</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$uid"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$statistics_type">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.statistics_type</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$statistics_type"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$query">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.query</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$query"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$scopeType">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.scopeType</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$scopeType"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$scopeId">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.scopeId</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$scopeId"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$rpp">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.rpp</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$rpp"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$sortBy">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.sortBy</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$sortBy"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$sortOrder">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.sortOrder</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$sortOrder"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$page">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.page</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$page"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$workflowStep">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.workflowStep</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$workflowStep"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$previousWorkflowStep">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.previousWorkflowStep</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$previousWorkflowStep"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$owner">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.owner</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$owner"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$submitter">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.submitter</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$submitter"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$actor">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.actor</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$actor"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
					<xsl:if test="$workflowItemId">
	    				<li>
	    					<span class="popup-label-element">
	    						<i18n:text>xmlui.ArtifactBrowser.StatisticsSimpleSearch.statistics_field.label.workflowItemId</i18n:text>
	    					</span>
	    					<xsl:text>: </xsl:text>
	    					<span class="popup-label-value"><xsl:value-of select="$workflowItemId"></xsl:value-of></span>
	    				</li>
    				</xsl:if>
    			</ul>
    		</div>
    	</span>
    </xsl:template>

</xsl:stylesheet>
