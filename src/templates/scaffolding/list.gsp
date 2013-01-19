<% import grails.persistence.Event %>
<% moduleName = domainClass.propertyName %>
<g:render template="/layouts/main/header" />
<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
<h1><g:message code="default.list.label" args="[entityName]" /></h1>
<div id="${moduleName}Main">
	<table id="${moduleName}List"></table>
	<div id="${moduleName}Pager"></div>
</div>
<script type="text/javascript">

var moduleName = "${domainClass.propertyName}";
var baseUrl = "\${application.getContextPath()}" + "/${domainClass.propertyName}/";

idFormatter = function(elLiner, oRecord, oColumn, oData) {
	var id = oData;
	var clickShow = "'" + 'loadData("' + moduleName + 'MainDiv", "' + baseUrl + 'show/' + id + '")' +"'";
	var clickEdit = "'" + 'loadData("' + moduleName + 'MainDiv", "' + baseUrl + 'edit/' + id + '")' +"'";
	elLiner.innerHTML = "<a  onclick=" + clickShow + " href='#'>Show</a>, <a  onclick=" + clickEdit + "href='#'>Edit</a>";
}
var myColumnDefs = [];
var colNames = [];
<%
excludedProps = Event.allEvents.toList() << 'version'
allowedNames = domainClass.persistentProperties*.name << 'id' << 'dateCreated' << 'lastUpdated'
props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) && !Collection.isAssignableFrom(it.type) }
Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
props.eachWithIndex { p, i ->
	def edittype = "text"
	if (p.name == "password") {
		edittype = "password"
	}
%>
	myColumnDefs[${i}] = { name: "${p.naturalName}", index: "${p.name}", sortable: true<% if (p.name != "id") { %>, editable: true, edittype: "${edittype}"<% } %> };
	colNames[${i}] = "${p.naturalName}";
<%
}
%>

var selectedRowId = null;
var dialog = \$("<div></div>");

\$(function(){
	\$("#${moduleName}List").jqGrid({
		url: baseUrl + "getData",
		datatype: 'json',
		mtype: 'GET',
		colNames: colNames,
		colModel: myColumnDefs,
		pager: '#${moduleName}Pager',
		rowList: [10, 20, 50, 100],
		sortname: 'id',
		sortorder: 'desc',
		viewrecords: true,
		autowidth: true,  
		caption: '${moduleName} list',
		editurl: baseUrl + "setData",
		loadComplete: function() {
			if (selectedRowId) {
				\$(this).jqGrid("setSelection", selectedRowId);
				selectedRowId = null;
			}
		},
		ondblClickRow: function(rowId, iRow, iCol, e) {
			if (rowId) {
				dialog.load(baseUrl + "edit?id=" + rowId).dialog({
					autoOpen: false,
					title: "Edit Record",
					width: "600px",
					position: "top"
				});
				dialog.dialog("open");
			}
		}
	});
	jQuery("#${moduleName}List").jqGrid("navGrid", "#${moduleName}Pager",
		{
			add: true,
			addfunc: function() {
				dialog.load(baseUrl + "create").dialog({
					autoOpen: false,
					title: "Create Record",
					width: "600px",
					position: "top"
				});
				dialog.dialog("open");
			},
			edit: true,
			editfunc: function(rowId) {
				dialog.load(baseUrl + "edit?id=" + rowId).dialog({
					autoOpen: false,
					title: "Edit Record",
					width: "600px",
					position: "top"
				});
				dialog.dialog("open");
			},
			view: true,
			del: true,
			search: true,
			refresh: true
		},
		{
			// Edit options (not used)
		},
		{
			// Add options (not used)
		},
		{
			// Delete options
			afterSubmit: afterSubmit
		}
	);
});

function afterSubmit(response, postdata) {
	var obj = jQuery.parseJSON(response.responseText);
	var success = true;
	var message = "";
	if (!obj) {
		success = false;
		message = "Unknown error";
	} else {
		errors = obj.errors;
		if (errors && errors.length) {
			success = false;
			for (error in errors) {
				message += errors[error].message + "<br/>";
			}
		}
	}
	return [success, message];
}

</script>
<g:render template="/layouts/main/footer" />
