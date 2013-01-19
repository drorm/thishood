<% import grails.persistence.Event %>
<% import org.codehaus.groovy.grails.plugins.PluginManagerHolder %>
<% moduleName = domainClass.propertyName %>
<g:render template="/layouts/main/header" />
<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
<g:if test="\${flash.message}">
<div class="message">\${flash.message}</div>
</g:if>
<g:hasErrors bean="\${${propertyName}}">
<div class="errors">
    <g:renderErrors bean="\${${propertyName}}" as="list" />
</div>
</g:hasErrors>
<g:form action="update" name="form1" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
    <g:hiddenField name="id" value="\${${propertyName}?.id}" />
    <g:hiddenField name="version" value="\${${propertyName}?.version}" />
    <div class="dialog">
        <table>
            <tbody>
            <%  excludedProps = Event.allEvents.toList() << 'version' << 'id' << 'dateCreated' << 'lastUpdated'
                persistentPropNames = domainClass.persistentProperties*.name
                props = domainClass.properties.findAll { persistentPropNames.contains(it.name) && !excludedProps.contains(it.name) }
                Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
                display = true
                boolean hasHibernate = PluginManagerHolder.pluginManager.hasGrailsPlugin('hibernate')
                props.each { p ->
                    if (hasHibernate) {
                        cp = domainClass.constrainedProperties[p.name]
                        display = (cp?.display ?: true)
                    }
                    if (display) { %>
                <tr class="prop">
                    <td valign="top" class="name">
                      <label for="${p.name}"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></label>
                    </td>
                    <td valign="top" class="value \${hasErrors(bean: ${propertyName}, field: '${p.name}', 'errors')}">
                        ${renderEditor(p)}
                    </td>
                </tr>
            <%  }   } %>
            </tbody>
        </table>
    </div>
    <div class="buttons">
        <span class="button"><g:actionSubmit class="save" action="update" value="\${message(code: 'default.button.update.label', default: 'Update')}" /></span>
<%/*
        <span class="button"><g:actionSubmit class="delete" action="delete" value="\${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
*/%>
    </div>
</g:form>
<script type="text/javascript">

\$(document).ready(function() {
	\$("#form1").parent().attr("id", "form1parent");
	var options = {
		target: "#form1parent",
		success: onSuccess
	};
	\$("#form1").submit(function() {
		\$(this).ajaxSubmit(options);
		return false;
	});
});

function onSuccess(responseText, statusText) {
	var grid = \$("#${moduleName}List");
	if (grid) {
		// Set global variable to restore selection after grid reload
		selectedRowId = grid.jqGrid("getGridParam", "selrow");
		grid.trigger("reloadGrid");
	}
}

</script>
<g:render template="/layouts/main/footer" />