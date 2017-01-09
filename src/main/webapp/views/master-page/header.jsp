<%--
 * header.jsp
 *
 * Copyright (C) 2014 Universidad de Sevilla
 * 
 * The use of this project is hereby constrained to the conditions of the 
 * TDG Licence, a copy of which you may download from 
 * http://www.tdg-seville.info/License.html
 --%>

<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="security" uri="http://www.springframework.org/security/tags"%>


<div id="cssmenu">
	<ul>
		<security:authorize access="isAnonymous()">
			
			<li><a href="http://auth-egc.azurewebsites.net/?returnUrl=http://localhost:8080/ADMCensus/welcome/index.do"><spring:message code="master.page.login" /></a></li>
			<li class='has-sub'><a><spring:message code="master.page.listar" /></a>
				<ul>
					<li><a href="census/votesByUser.do?token=${token}"><spring:message code="master.page.census.activeVotes" /></a></li>
					<li><a href="census/getAllCensusByCreador.do?token=${token}"><spring:message code="master.page.census.byCreator" /></a></li> 
					<li><a href="census/getCensusesToRegister.do"><spring:message code="master.page.census.listRegister" /></a></li>
					<li><a href="census/getFinishedCensus.do"><spring:message code="master.page.census.finishedCensus" /></a></li>
				</ul>
			</li>
			<li class='has-sub'><a href="http://localhost:8080/CreacionAdminVotaciones"><spring:message code="master.page.votaciones" /></a></li>
			<li class='has-sub'><a href="http://localhost:8080/results_view"><spring:message code="master.page.resultados" /></a></li>
			<li class='has-sub'><a href="http://localhost:8080/Deliberations"><spring:message code="master.page.deliberaciones" /></a></li>
			<li><a href="https://recuento.herokuapp.com/"><spring:message code="master.page.recuento" /></a></li>
			<li><a href="http://storage-egc1516.rhcloud.com/"><spring:message code="master.page.almacenamiento" /></a></li>
			<li><a href="http://localhost:8080/ADMCensus/welcome/index.do?token=logout"><spring:message code="master.page.logout" /></a></li>
		</security:authorize>
	</ul>
</div>


