<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->

<!--
    Rendering of the authority control related pages.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->
<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:xmlui="xalan://ar.edu.unlp.sedici.dspace.xmlui.util.XSLTHelper" 
	xmlns:confman="org.dspace.core.ConfigurationManager"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc xmlui">

    <xsl:output indent="yes"/>
    
    <xsl:template match="dri:field[@type='composite']/dri:field[@id='aspect.administrative.item.EditItemMetadataForm.field.value_confidence']" mode="compositeComponent">    	
    	
    	<xsl:apply-templates select="."/>
    	
    	<xsl:call-template name="authorityConfidenceIcon">
    		<xsl:with-param name="id">aspect_administrative_item_EditItemMetadataForm_field_value_confidence_indicator</xsl:with-param>
    	</xsl:call-template>
    	
    	<xsl:variable name="LookupFunction">
    		<xsl:text>javascript:DSpaceChoiceLookup('</xsl:text>
	          <!-- URL -->
	          <xsl:value-of select="concat($context-path,'/admin/lookup')"/>
	          <xsl:text>', '</xsl:text>
	          <!-- metadataName --> <!-- This field will be updated by JQuery actions -->
	          <xsl:value-of select="'|metadata_name_to_replace|'"/>
	          <xsl:text>', '</xsl:text>
	          <!-- formID -->
	          <xsl:value-of select="'aspect_administrative_item_EditItemMetadataForm_div_edit-item-status'"/>
	          <xsl:text>', '</xsl:text>
	          <!-- valueInput -->
	          <xsl:value-of select="'value'"/>
	          <xsl:text>', '</xsl:text>
	          <!-- authorityInput, name of field to get authority -->
	          <xsl:value-of select="'value_authority'"/>
	          <xsl:text>', '</xsl:text>
	          <!-- Confidence Indicator's ID so lookup can frob it -->
	          <xsl:value-of select="'aspect_administrative_item_EditItemMetadataForm_field_value_confidence_indicator'"/>
	          <xsl:text>', </xsl:text>
	          <!-- Collection ID for context -->
	          <xsl:choose>
	            <xsl:when test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='choice'][@qualifier='collection']">
	            	<xsl:text>'</xsl:text>
	              	<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='choice'][@qualifier='collection']"/>
	              	<xsl:text>'</xsl:text>
	            </xsl:when>
	            <xsl:otherwise>
	              <xsl:text>-1</xsl:text>
	            </xsl:otherwise>
	          </xsl:choose>
	          <xsl:text>, </xsl:text>
	          <!-- isName -->
	          <xsl:value-of select="false()"/>
	          <xsl:text>, </xsl:text>
	          <!-- isRepating -->
	          <xsl:value-of select="false()"/>
	          <xsl:text>);</xsl:text>
    	</xsl:variable>
    	
    	<script type="text/javascript">
    		/*
    		** This function let select an authority in the "Edit Item" view and "Create/Edit Collection Template" view, when
    		** you wants to modify or add a new metadata to the item or template item. 
    		**
    		*/
    		function updateMetadataForLookup(){
				//Hide the confidence field and make authority readonly...
				$('#aspect_administrative_item_EditItemMetadataForm_field_value_confidence').hide();
				$('#aspect_administrative_item_EditItemMetadataForm_field_value_authority').attr('readonly',true);
		
				//Update the metadata used for lookup authorities in the "Lookup" button
				$('#aspect_administrative_item_EditItemMetadataForm_field_field').change(updateLookupButtonState);
				
				//Initialize lookup button avoiding synchronization issues between "Lookup button" and "Metadata Selector"
				updateLookupButtonState();
				
				//Apply some CSS at input fields
				inputsStyleSheet()
				
				//Initialize language field with default repository language
				$('#aspect_administrative_item_EditItemMetadataForm_field_language').val('<xsl:value-of select="confman:getProperty('default.locale')"/>');
			}
				
			function getLookupFunction(metadataNameForLookUp){
				return "<xsl:value-of select="$LookupFunction"/>".replace('|metadata_name_to_replace|',metadataNameForLookUp);
			}
			
			function updateLookupButtonState(){
				var metadataSelected = $('#aspect_administrative_item_EditItemMetadataForm_field_field option:selected').text().replace(/\./g,'_');
					
				$("input[type='button'][name='lookup_value']").attr('onclick', getLookupFunction(metadataSelected));
				
				//Reset authority field and value field values
				$('#aspect_administrative_item_EditItemMetadataForm_field_value_authority').val('');
				$('#aspect_administrative_item_EditItemMetadataForm_field_value').val('');
				
				if(isAuthorityControlled(metadataSelected.replace(/\_/g,'.'))){
					$("input[type='button'][name='lookup_value']").removeAttr('disabled');
				}else{
					$("input[type='button'][name='lookup_value']").attr('disabled',true);
				}
			}
			
			function isAuthorityControlled(fieldName){
				var authorityControlledFields=[
				<xsl:for-each select="xmlui:getPropertyKeys('authority.controlled')">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="xmlui:replaceAll(.,'authority\.controlled\.','')"/>
					<xsl:if test="position() != last()">
						<xsl:text>", </xsl:text>
					</xsl:if>
				</xsl:for-each>"];
				<xsl:text disable-output-escaping="yes">return (authorityControlledFields.indexOf(fieldName) >= 0);</xsl:text>
			}
	        
	        <!-- Modificación de estilos en edicion de metadatos del item -->
			function inputsStyleSheet(){			
	               var metadataFormValueParent = $("#aspect_administrative_item_EditItemMetadataForm_field_value").parent();
	               var metadataFormValue = $("#aspect_administrative_item_EditItemMetadataForm_field_value");
	               var inputButton = $("#aspect_administrative_item_EditItemMetadataForm_field_value input");
	
	               metadataFormValueParent.css('width','100%');
	               metadataFormValue.css('width','80%');
	               metadataFormValue.css('float','left');
	               inputButton.css('float','left','important');
	               inputButton.css('margin-left','10px');
	        }
    	</script>
      
    </xsl:template>
    
</xsl:stylesheet>