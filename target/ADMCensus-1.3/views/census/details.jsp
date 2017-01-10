<%--
 * action-2.jsp
 *
 * Copyright (C) 2013 Universidad de Sevilla
 * 
 * The use of this project is hereby constrained to the conditions of the 
 * TDG Licence, a copy of which you may download from 
 * http://www.tdg-seville.info/License.html
 --%>

<%@page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="acme" tagdir="/WEB-INF/tags"%>

<spring:message code="census.token_propietario" />
:
<jstl:out value="${census.username}" />
<br />

<spring:message code="census.tipo" />
:
<jstl:if test="${census.tipoCenso == 'abierto' }">
	<spring:message code="census.abierto" />
	<br />
</jstl:if>

<jstl:if test="${census.tipoCenso == 'cerrado' }">
	<spring:message code="census.cerrado" />
	<br />
</jstl:if>

<spring:message code="census.votacio.name" />
:
<jstl:out value="${census.tituloVotacion}" />
<br />

<spring:message code="census.fecha.inicio" />
:
<fmt:formatDate value="${census.fechaInicioVotacion}"
	pattern="dd/MM/yyyy" />
<br />

<spring:message code="census.fecha.fin" />
:
<fmt:formatDate value="${census.fechaFinVotacion}" pattern="dd/MM/yyyy" />
<br />

<jstl:if test="${editable}">
	<acme:cancel url="census/edit.do?censusId=${census.id }"
		code="census.edit" />
</jstl:if>

<acme:cancel url="census/export.do?censusId=${census.id}"
	code="census.export" />


<acme:cancel url="census/getAllCensusByCreador.do" code="census.back" />

