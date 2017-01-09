<%@page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="acme" tagdir="/WEB-INF/tags"%>

<display:table pagesize="5" class="displaytag" name="censuses"
	requestURI="${requestURI}" id="row">


	<!-- Attributes -->
	<spring:message code="census.token_propietario" var="username" />
	<display:column property="username" title="${username}" sortable="true" />

	<spring:message code="census.votacio.name" var="tituloVotacion" />
	<display:column property="tituloVotacion" title="${tituloVotacion}"
		sortable="true" />

	<spring:message code="census.fecha.inicio" var="fechaInicioVotacion" />
	<display:column title="${fechaInicioVotacion}" sortable="true">
		<fmt:formatDate value="${row.fechaInicioVotacion}"
			pattern="dd/MM/yyyy" />
		<br />
	</display:column>

	<spring:message code="census.fecha.fin" var="fechaFinVotacion" />
	<display:column title="${fechaFinVotacion}" sortable="true">
		<fmt:formatDate value="${row.fechaFinVotacion}" pattern="dd/MM/yyyy" />
		<br />
	</display:column>

	<spring:message code="census.number.person" var="sizeHeader" />
	<display:column title="${sizeHeader}" sortable="true">
		<jstl:out value="${row.votoPorUsuario.size() }"></jstl:out>
	</display:column>

	<spring:message code="census.register" var="registerHeader" />
	<display:column title="${registerHeader}" sortable="false">
		<a href="census/registerUser.do?censusId=${row.id }"><spring:message
				code="census.register"></spring:message></a>
	</display:column>


</display:table>
<br />
<acme:cancel url="welcome/index.do" code="census.back" />
