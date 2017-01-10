<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@taglib prefix="jstl"	uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="acme" tagdir="/WEB-INF/tags" %>

<div id="form-wrapper" class="myform">
	<div class="divCenter">
	
	
<form:form id="form" action="census/edit.do" modelAttribute="census">

	<form:hidden path="id" />
	<form:hidden path="version" />
	<form:hidden path="tokenPropietario" />
	<form:hidden path="votoPorUsuario"/>
	<form:hidden path="votacionId"/>
	
	
	
	<acme:textbox code="census.name" path="name"/>
	
	<acme:submit name="save" code="census.save"/>

</form:form>

<acme:cancel url="welcome/index.do" code="census.back"/>
	
	</div>
</div>


