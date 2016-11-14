<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/" xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/" 
		xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc ">


	<xsl:template match="dri:div[@id='aspect.ELProcessor.SelectionPage.div.contact']">
		<form id="home-search-form" action="executePreConfigQuery" class="form-inline" role="form">
<!-- 			<select name="propertyName" id="propertyName"> -->
<!-- 				<xsl:for-each select="dri:list[@id='aspect.ELProcessor.SelectionPage.list.options']/dri:item">				 -->
<!-- 						<option> -->
<!-- 							<xsl:attribute name="value"> -->
<!-- 				    			<xsl:value-of select="dri:field[@n='identifier']"></xsl:value-of> -->
<!-- 				    		</xsl:attribute> -->
<!-- 				    		<xsl:value-of select="dri:field[@n='description']"></xsl:value-of> -->
<!-- 						</option>							 -->
<!-- 				</xsl:for-each> -->
<!-- 			</select> -->
			<input name="query" id="query"></input>
			<button type="submit" name="lr" class="btn btn-link">Ejecutar</button>
		</form>
	</xsl:template>
</xsl:stylesheet>