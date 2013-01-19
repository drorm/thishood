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
<g:form action="save" name="form1"  <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
	<div id="error"></div>
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
                    if (!Collection.class.isAssignableFrom(p.type)) {
                        if (hasHibernate) {
                            cp = domainClass.constrainedProperties[p.name]
                            display = (cp ? cp.display : true)
                        }
                        if (display) { %>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="${p.name}"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></label>
                    </td>
                    <td valign="top" class="value \${hasErrors(bean: ${propertyName}, field: '${p.name}', 'errors')}">
                        ${renderEditor(p)}
						<%/*
						${domainClass.constraints[p.name]}
						*/%>
                    </td>
                </tr>
            <%  }   }   } %>
            </tbody>
        </table>
    </div>
    <div class="buttons">
        <span class="button"><g:submitButton name="create" class="save" value="\${message(code: 'default.button.create.label', default: 'Create')}" /></span>
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
		grid.trigger("reloadGrid");
	}
}
	
</script>
<g:render template="/layouts/main/footer" />