<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
template for our sample md-toast
-->
	
<md-toast style="width: 600px; min-height: 250px; height: 250px; background-color: #bbb; text-align: left; align-items: initial; padding: 0px; z-index: 1000;">

	<md-content class="md-hue-1" layout="column" style="display: flex; flex-flow: column; height: 100%; width: 100%;">

		this is a test of the<br>
		emergency broadcast system<br>

		<span flex>Custom toast!</span>
		<md-button ng-click="closeToast()" style="color: #000;">
		Close
		</md-button>

	</md-content>

</md-toast>