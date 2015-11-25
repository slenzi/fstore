<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
 
<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%--
Need for Spring Security Cross Site Requests Forgery (CSRF) protection. POST requests will fail without this.
The CSRF token also needs to be set in the header for AngularJS AJAX $HTTP/$RESOURCE requests.

By default AngularJS provides a mechanism to implement Cross Site Request Forgery, however this mechanism works 
with cookies only. Since Spring Security works by setting a token as an HTTP parameter, the out of the box 
solution AngularJS provides wouldn't work.

http://stackoverflow.com/questions/32419923/angularjs-and-spring-security-give-405-error-when-enable-csrf
--%>
<!-- For Spring Security Cross Site Requests Forgery (CSRF) protection -->
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>