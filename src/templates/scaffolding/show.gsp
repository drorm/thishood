<% import grails.persistence.Event %>
<% moduleName = domainClass.propertyName %>
<g:render template="/layouts/main/header" />
<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
<g:if test="\${flash.message}">
	<div class="message">\${flash.message}</div>
</g:if>
<div class="dialog">
    <table>
        <tbody>
        <%  excludedProps = Event.allEvents.toList() << 'version'
            allowedNames = domainClass.persistentProperties*.name << 'id' << 'dateCreated' << 'lastUpdated'
            props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) }
            Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
            props.each { p -> %>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></td>
                <%  if (p.isEnum()) { %>
                <td valign="top" class="value">\${${propertyName}?.${p.name}?.encodeAsHTML()}</td>
                <%  } else if (p.oneToMany || p.manyToMany) { %>
                <td valign="top" style="text-align: left;" class="value">
                    <ul>
                    <g:each in="\${${propertyName}.${p.name}}" var="${p.name[0]}">
                        <li><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${p.name[0]}.id}">\${${p.name[0]}?.encodeAsHTML()}</g:link></li>
                    </g:each>
                    </ul>
                </td>
                <%  } else if (p.manyToOne || p.oneToOne) { %>
                <td valign="top" class="value"><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${propertyName}?.${p.name}?.id}">\${${propertyName}?.${p.name}?.encodeAsHTML()}</g:link></td>
                <%  } else if (p.type == Boolean.class || p.type == boolean.class) { %>
                <td valign="top" class="value"><g:formatBoolean boolean="\${${propertyName}?.${p.name}}" /></td>
                <%  } else if (p.type == Date.class || p.type == java.sql.Date.class || p.type == java.sql.Time.class || p.type == Calendar.class) { %>
                <td valign="top" class="value"><g:formatDate date="\${${propertyName}?.${p.name}}" /></td>
                <%  } else if(!p.type.isArray()) { %>
                <td valign="top" class="value">\${fieldValue(bean: ${propertyName}, field: "${p.name}")}</td>
                <%  } %>
            </tr>
        <%  } %>
        </tbody>
    </table>
</div>
<%/*

<div class="buttons">
    <g:form name="form1">
        <g:hiddenField name="id" value="\${${propertyName}?.id}" />
        <span class="button"><g:actionSubmit class="edit" action="edit" value="\${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
        <span class="button"><g:actionSubmit class="delete" action="delete" value="\${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
    </g:form>
</div>
<script type="text/javascript">

\$(document).ready(function() {
	var options = {
		target: "#" + \$("#form1").parent().parent().attr("id")
	};
	\$("#form1").submit(function() {
		\$(this).ajaxSubmit(options);
		return false;
	});
});

</script>

*/%>
<g:render template="/layouts/main/footer" />