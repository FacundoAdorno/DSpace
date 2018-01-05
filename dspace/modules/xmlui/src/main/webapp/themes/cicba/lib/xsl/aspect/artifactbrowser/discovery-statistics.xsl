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
            </xsl:choose>
            <xsl:choose>
            	<xsl:when test="$type='search'">
            		<xsl:call-template name="searchStatisticsType"/>
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
            <div class="col-md-12">
            <span>IP de acceso:  <xsl:value-of select="$ip"/> (<xsl:value-of select="$city"/>, <xsl:value-of select="$country"/>)</span>
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
	           <div class="col-md-12">
	           	<span>IP de acceso:  <xsl:value-of select="$ip"/> (<xsl:value-of select="$city"/>, <xsl:value-of select="$country"/>)</span>
	           </div>
	       </div>
           <!-- tercera fila (TERMINOS DE BUSQUEDA) -->
           <!-- TODO implementar -->
    </xsl:template>

</xsl:stylesheet>
