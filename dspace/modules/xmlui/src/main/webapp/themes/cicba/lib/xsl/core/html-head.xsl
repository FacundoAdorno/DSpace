<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:confman="org.dspace.core.ConfigurationManager"
    xmlns:xmlui="xalan://ar.edu.unlp.sedici.dspace.xmlui.util.XSLTHelper"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc confman">

	<!-- The HTML head element contains references to CSS as well as embedded JavaScript code. Most of this
        information is either user-provided bits of post-processing (as in the case of the JavaScript), or
        references to stylesheets pulled directly from the pageMeta element. -->
    <xsl:template name="buildHead">
	
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

            <!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

            <!--  Mobile Viewport Fix
                  j.mp/mobileviewport & davidbcalhoun.com/2010/viewport-metatag
            device-width : Occupy full width of the screen in its current orientation
            initial-scale = 1.0 retains dimensions instead of zooming out if page height > device height
            maximum-scale = 1.0 retains dimensions instead of zooming in if page width < device width
            -->
            <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;"/>
			<link rel="search" type="application/opensearchdescription+xml" title="CIC para Firefox" href="http://digital.cic.gba.gob.ar/moz-search-plugin.xml"/>
            <link rel="shortcut icon">
                <xsl:attribute name="href">
                    <xsl:call-template name="print-theme-path">
                    	<xsl:with-param name="path">images/favicon.ico</xsl:with-param>
                    </xsl:call-template>
                </xsl:attribute>
            </link>
            <link rel="apple-touch-icon">
                <xsl:attribute name="href">
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/images/favicon.ico</xsl:text>
                </xsl:attribute>
            </link>

            <meta name="Generator">
              <xsl:attribute name="content">
                <xsl:text>DSpace</xsl:text>
                <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']"/>
                </xsl:if>
              </xsl:attribute>
            </meta>
            
			
			            
            <!-- Add stylesheets -->


			<xsl:for-each
				select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="media">
		            	<xsl:value-of select="@qualifier" />
		            </xsl:attribute>
					<xsl:attribute name="href">
		                <xsl:call-template name="print-theme-path" >
		                	<xsl:with-param name="path"><xsl:value-of select="." /></xsl:with-param>
		                </xsl:call-template>
		            </xsl:attribute>
				</link>
			</xsl:for-each>
			
             <link rel="stylesheet" href="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.css" />
      		 <link  rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"/>
      		 <script src="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.js">&#160;</script>
      		 

            <!-- Add syndication feeds -->
            <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
                <link rel="alternate" type="application">
                    <xsl:attribute name="type">
                        <xsl:text>application/</xsl:text>
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </link>
            </xsl:for-each>

            <!--  Add OpenSearch auto-discovery link -->
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']">
                <link rel="search" type="application/opensearchdescription+xml">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='scheme']"/>
                        <xsl:text>://</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverName']"/>
                        <xsl:text>:</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverPort']"/>
                        <xsl:value-of select="$context-path"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='autolink']"/>
                    </xsl:attribute>
                    <xsl:attribute name="title" >
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']"/>
                    </xsl:attribute>
                </link>
            </xsl:if>
            
            <!-- Add the title in -->
			<xsl:call-template name="addPageTitle"/>            

            <!-- Head metadata in item pages -->
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']">
                <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']"
                              disable-output-escaping="yes"/>
            </xsl:if>

            <!-- Add all Google Scholar Metadata values -->
            <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[substring(@element, 1, 9) = 'citation_']">
                <meta name="{@element}" content="{.}"></meta>
            </xsl:for-each>
            
            <!-- Add MathJAX JS library to render scientific formulas-->
            <xsl:if test="confman:getProperty('webui.browse.render-scientific-formulas') = 'true'">
                <script type="text/x-mathjax-config">
                    MathJax.Hub.Config({
                      tex2jax: {
                        inlineMath: [['$latex','$'], ['\\(','\\)']],
                        ignoreClass: "detail-field-data|detailtable|exception"
                      },
                      TeX: {
                        Macros: {
                          AA: '{\\mathring A}'
                        }
                      }
                    });
                </script>
                <script type="text/javascript" src="//cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML">&#160;</script>
            </xsl:if>
            
            <!-- The following javascript removes the default text of empty text areas when they are focused on or submitted -->
        <!-- There is also javascript to disable submitting a form when the 'enter' key is pressed. -->
        <script type="text/javascript">
                        //Clear default text of empty text areas on focus
                        function tFocus(element)
                        {
                                if (element.value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){element.value='';}
                        }
                        //Clear default text of empty text areas on submit
                        function tSubmit(form)
                        {
                                var defaultedElements = document.getElementsByTagName("textarea");
                                for (var i=0; i != defaultedElements.length; i++){
                                        if (defaultedElements[i].value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){
                                                defaultedElements[i].value='';}}
                        }
                        //Disable pressing 'enter' key to submit a form (otherwise pressing 'enter' causes a submission to start over)
                        function disableEnterKey(e)
                        {
                             var key;

                             if(window.event)
                                  key = window.event.keyCode;     //Internet Explorer
                             else
                                  key = e.which;     //Firefox and Netscape

                             if(key == 13)  //if "Enter" pressed, then disable!
                                  return false;
                             else
                                  return true;
                        }

                        function FnArray()
                        {
                            this.funcs = new Array;
                        }

                        FnArray.prototype.add = function(f)
                        {
                            if( typeof f!= "function" )
                            {
                                f = new Function(f);
                            }
                            this.funcs[this.funcs.length] = f;
                        };

                        FnArray.prototype.execute = function()
                        {
                            for( var i=0; i <xsl:text disable-output-escaping="yes">&lt;</xsl:text> this.funcs.length; i++ )
                            {
                                this.funcs[i]();
                            }
                        };

                        var runAfterJSImports = new FnArray();
            </script>
            <xsl:if test="/dri:document/dri:body/dri:div[@id='aspect.submission.StepTransformer.div.submit-describe']/dri:list[@id='aspect.submission.StepTransformer.list.submit-describe']">
				<script type="text/javascript" src="//cdn.ckeditor.com/4.4.7/full/ckeditor.js">&#160;</script>
				<script type="text/javascript">var path= "<xsl:value-of select="$context-path"/>";
				CKEDITOR.config.customConfig =path.concat("/static/js/editorConfig.js");					
				CKEDITOR.config.language= "<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='page' and @qualifier='currentLocale']"/>";
				</script>
			</xsl:if>
			
			<!-- Si estamos renderizando la página de statistics-discover, entonces mostramos el javascript -->
			<xsl:if test="/dri:document/dri:body/dri:div[@id='aspect.discovery.StatisticsSimpleSearch.div.search']">
				<!-- Primero importamos los dependencias y scripts necesarios para ejecutar C3 -->
				<script type="text/javascript">
					<xsl:attribute name="src">
		                <xsl:call-template name="print-theme-path" >
		                	<xsl:with-param name="path">js/d3.js</xsl:with-param>
		                </xsl:call-template>
		            </xsl:attribute>
		            &#160;
				</script>
			    <link rel="stylesheet" type="text/css">
			    	<xsl:attribute name="href">
		                <xsl:call-template name="print-theme-path" >
		                	<xsl:with-param name="path">css/c3.css</xsl:with-param>
		                </xsl:call-template>
		            </xsl:attribute>
		            &#160;
			    </link>
			    <script type="text/javascript">
					<xsl:attribute name="src">
		                <xsl:call-template name="print-theme-path" >
		                	<xsl:with-param name="path">js/c3.js</xsl:with-param>
		                </xsl:call-template>
		            </xsl:attribute>
		            &#160;
				</script>
				
				<script type="text/javascript">
					<xsl:attribute name="src">
		                <xsl:call-template name="print-theme-path" >
		                	<xsl:with-param name="path">js/statistics-discovery.js</xsl:with-param>
		                </xsl:call-template>
		            </xsl:attribute>
		            &#160;
				</script>
			    
			</xsl:if>
	</xsl:template>
	
	<xsl:template name="addPageTitle">
		<xsl:variable name="page_title" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']" />
            <title>
                <xsl:choose>
                        <xsl:when test="starts-with($request-uri, 'page/')">
                                <i18n:text>
                                	<xsl:value-of select="concat('xmlui.cicdigital.title.', xmlui:replaceAll(substring-after($request-uri,'/'), '(_en|_es)', ''))"/>
                                </i18n:text>
                        </xsl:when>
                        <xsl:when test="not($page_title)">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </xsl:when>
                        <xsl:when test="$page_title = ''">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </xsl:when>
                        <xsl:otherwise>
                                <xsl:copy-of select="$page_title/node()" />
                        </xsl:otherwise>
                </xsl:choose>
            </title>
	</xsl:template>
</xsl:stylesheet>