/**
 * Funciones que controlan aspectos de la vista en statistics-discovery
 */
function loadDateTimePickers(){
	$('input[type="text"].discovery-filter-input.date-input').datetimepicker({
		dateFormat: 'yy-mm-dd',
		timeFormat: "HH:mm:ss.l'Z'",
		separator: 'T',
		changeYear: true,
		changeMonth: true
	});
}

function loadChartTabs(){
	$('#aspect_discovery_StatisticsSimpleSearch_div_statistics-discovery-chart-section p:first').after('<div id="tabs_chart"></div>');
	var tabMainDiv = $('div#tabs_chart');
	$(tabMainDiv).append('<ul><li><a href="#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_onevar_chart_options">Reporte por campo de registro</a></li><li><a href="#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options">Reporte por campo de registro condicionado</a></li></ul>');
	$('div#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_onevar_chart_options').appendTo($(tabMainDiv));
	$('div#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options').appendTo($(tabMainDiv));
	$(tabMainDiv).tabs();
}

/**
* Funciones que se encarga de renderizar los graficos de Statistics-Discovery usando c3.js
**/
function updateC3Chart(JsonDataURL,typeOfChart){
    var DEFAULT_MIN_RESULTS = 1;
    var spinnerContainer;
    var onevarUpdateBttn = $('input[name="statistics_onevar_report_bttn"]');
    var twovarsonefixedUpdateBttn = $('input[name="statistics_twovarsonefixed_report_bttn"]');
    if(typeOfChart == 'onevar'){
        var countof = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_onevar_chart_options select[name="countof"]').val();
        var timelapse = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_onevar_chart_options select[name="timelapse"]').val();
        var minResults = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_onevar_chart_options input[name="min_results"]').val();
        spinnerContainer = onevarUpdateBttn;
        
    }
    if(typeOfChart == 'twovars-onefixed'){
        var countof = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options select[name="countof"]').val();
        var timelapse = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options select[name="timelapse"]').val();
        var by = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options select[name="by"]').val();
        var minResults = $('#aspect_discovery_StatisticsSimpleSearch_div_statistics_discovery_twovarsonefixed_chart_options input[name="min_results"]').val();
        spinnerContainer = twovarsonefixedUpdateBttn;
    }
    createSpinnerAt(spinnerContainer, [onevarUpdateBttn, twovarsonefixedUpdateBttn]);
    
    //agregamos parametros 'countof', 'timelapse' y 'by', en el caso que aplique
    if(countof != null && countof.length > 0){
        JsonDataURL += "&countof=" + countof;
    } else {
        //El parametro 'countof' es obligatorio para todos los tipos de reportes
        console.error("The parameter 'countof' must be set to graphicate the report!!")
        return;
    }
    if(by != null && by.length > 0){
        JsonDataURL += "&by=" + by;    
    }    
    if(timelapse != null && timelapse.length > 0){
        JsonDataURL += "&timelapse=" + timelapse;
    }
    //Chequeamos si el valor puesto en "min_results" es un numero, sino le ponemos el valor por defecto
    if(minResults.match(/^\d+$/)){
        minResults = Number(minResults);
    } else {
        minResults = DEFAULT_MIN_RESULTS;
    }
    JsonDataURL += "&minresults=" + minResults;
    //Cuando la consulta al endpoint JSON termine exitosamente (.done(...)), entonces graficamos el chart
    $.getJSON(JsonDataURL).done(function(json_data){
//        console.log(json_data);
        //Generamos los arreglos de datos que espeta c3.js para dibujar el chart
        var columnData = [];
        if(timelapse == null || timelapse.length == 0){
            columnData.push(["label", json_data.report_name])
        }
        for (var property in json_data.data) {
            if (json_data.data.hasOwnProperty(property)) {
                
            	var item = [removeControlCharacters(property)];
                var arrayLength = json_data.data[property].length;
                var value;
                for (var i = 0; i < arrayLength; i++) {
                    value = json_data.data[property][i];
                    item.push(value);
                }
                columnData.push(item);
            }
        }
        
        //Destruimos el spinner
        destroySpinnerAt(spinnerContainer, [onevarUpdateBttn, twovarsonefixedUpdateBttn]);
        
        //Update the c3.js chart at div#chart position
        var x_axis;
        var chart_type;
        var x_label_element;
        var groupedTooltip = (columnData.length <= 100);
        //Si hay mas de 200 columnas de datos, entonces no mostramos la leyenda inferior del gráfico
        var showLegend = (columnData.length <= 200);
        var chartTitle = {};
        if(timelapse != null && timelapse.length > 0){
        	var dateFormat;
        	if(timelapse == "month"){
        		dateFormat = '%Y-%m-%d';
        	} else {
        		dateFormat = '%Y';
        	}
        	//Si columnData[1] es vacío, es porque no hay datos
        	if(typeof columnData[1] != 'undefined'){
	        	//Nos fijamos el primer arreglo dentro de "columnData". Si solo tiene 2 elementos (p.e. ["La Plata", 100]) entonces mostramos el grafico como de barras
	        	if(columnData[1].length == 2){
	        		chart_type = "bar";
	        	} else {
	        		chart_type = "area";
	        	}
        	} else {
        		chart_type = "bar";
        	}
        	
            x_axis = 
                {
                    type: 'timeseries',
                    tick: {
                        format: dateFormat
                    }
                };
            x_label_element = "dateLabel";
            chartTitle = { text: json_data.report_name};
        } else {
            x_axis = 
                {
                    type: 'category'
                };
            x_label_element = "label";
            chart_type = "bar";
        }
        
        if(typeof columnData[1] == 'undefined'){
        	chartTitle.text = chartTitle.text + " (NO EXISTEN DATOS)"
        }
        
        chart = c3.generate({
            bindto: "#chart",
            data: {
                x: x_label_element,
                columns: columnData,
    			type: chart_type,
            },
            axis: {
                x: x_axis
            },
            size: {
		        width: 1000,
		        height: 500
		    },
            zoom: {
                enabled: true,
            },
            tooltip: {
            	grouped: groupedTooltip,
            },
            title: chartTitle,
            legend: {
                show: showLegend
            }
        });
    });
}
/**
 * Los caracters de control son invisibles y deben ser eliminados para evitar errores durante la generación de los charts en c3.js.
 * Mas información en https://stackoverflow.com/questions/21284228/removing-control-characters-in-utf-8-string
 */
function removeControlCharacters(label){
	return label.replace(/[\x00-\x1F\x7F-\x9F]/g, "");
}

function createSpinnerAt(containerElement,buttons) {
	$(containerElement).after('<span class="glyphicon glyphicon-repeat normal-right-spinner" title="Loading Report..." > </span>');
	for(var buttonPos in buttons){
		$(buttons[buttonPos]).attr("disabled",true);
	}
}

function destroySpinnerAt(containerElement,buttons){
	containerElement.siblings('.normal-right-spinner').remove();
	for(var buttonPos in buttons){
		$(buttons[buttonPos]).removeAttr("disabled");
	}
}
	
// When the user clicks on div, open the popup
function showPopUp(popupIdToShow) {
	if($('#' + popupIdToShow).hasClass("show")){
		$('#' + popupIdToShow).toggleClass("show");
	} else {
		$('.popup .popuptext').removeClass("show");
		$('#' + popupIdToShow).toggleClass("show");
	}
}

//Agregamos botones para excluir facetings
function addExcludeFacetBttns(){
	$("li.sidebar_facet_value").each(function (){
		var facetHref = $(this).find("a").attr("href");
		if(typeof facetHref != 'undefined' && facetHref != '' && facetHref.indexOf("\/statistics-search-filter") === -1){
			facetHref = facetHref.replace("filter_relational_operator=equals", "filter_relational_operator=notequals");
			$(this).append('<a class="bttn-remove" href="' + facetHref + '" title="Exclude result from faceting"><i class="glyphicon glyphicon-remove-sign"> <i> </a>');
		}
	});
}
