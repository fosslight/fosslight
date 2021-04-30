<%@ page language="java" session="true" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ taglib prefix="ct" uri="/WEB-INF/tlds/common-taglibs.tld" %>
<%@ taglib prefix="layer" uri="/WEB-INF/tlds/layer-taglibs.tld" %>
<%@ taglib prefix="paginator" uri="/WEB-INF/tlds/paginator.tld" %>

<c:set var="ctxPath" value="${pageContext.request.contextPath eq '/' ? '' : pageContext.request.contextPath}" />
<c:set var="remoteURI" value="${ctxPath}${requestScope['javax.servlet.forward.servlet_path']}" />
<c:set var="now" value="<%=new java.util.Date()%>" />
<fmt:formatDate value='${now}' pattern='yyyyMMdd' var="today"/>

<%-- Javascript Cache를 막기위한 Version 추가 --%>
<c:set var="jsVersion" value="${today}" />
<%-- Css Cache를 막기위한 Version 추가 --%>
<c:set var="cssVersion" value="${today}" />