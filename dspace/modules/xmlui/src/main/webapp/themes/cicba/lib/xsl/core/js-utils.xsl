<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

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
    
    <xsl:template name="print-is-authority-controlled-fn">
        function isAuthorityControlled(fieldName){
                var authorityControlledFields=[<xsl:call-template name="print_auth_controlled_fields"/>];
                <xsl:text disable-output-escaping="yes">return (authorityControlledFields.indexOf(fieldName) >= 0);</xsl:text>
            }
    </xsl:template>

    <xsl:template name="print-is-authority-required-fn">
        function isAuthorityRequired(fieldName){
                var authorityRequiredFields=[<xsl:call-template name="print_auth_required_fields"/>];
                <xsl:text disable-output-escaping="yes">return (authorityRequiredFields.indexOf(fieldName) >= 0);</xsl:text>
            }
    </xsl:template>
    
    <xsl:template name="print-get-authority-controlled-fieldnames-fn">
        function getAuthorityControlledFieldnames(){
                var authorityControlledFields=[<xsl:call-template name="print_auth_controlled_fields"/>];
                return authorityControlledFields;
            }
    </xsl:template>

    <xsl:template name="print-get-authority-required-fieldnames-fn">
        function getAuthorityRequiredFieldnames(){
                var authorityRequiredFields=[<xsl:call-template name="print_auth_required_fields"/>];
                return authorityRequiredFields;
            }
    </xsl:template>
    
    <xsl:template name="print_auth_controlled_fields">
        <xsl:for-each select="xmlui:getPropertyKeys('authority.controlled')">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="xmlui:replaceAll(.,'authority\.controlled\.','')"/>
            <xsl:if test="position() != last()">
                <xsl:text>", </xsl:text>
            </xsl:if>
        </xsl:for-each>"
    </xsl:template>

    <xsl:template name="print_auth_required_fields">
        <xsl:for-each select="xmlui:getPropertyKeysWithValue('authority.required', 'true')">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="xmlui:replaceAll(.,'authority\.required\.','')"/>
            <xsl:if test="position() != last()">
                <xsl:text>", </xsl:text>
            </xsl:if>
        </xsl:for-each>"
    </xsl:template>
    
</xsl:stylesheet>