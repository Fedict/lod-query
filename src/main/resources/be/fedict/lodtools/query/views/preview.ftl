<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<style>
<#include "style.css">
</style>
<title>List of reconciliation services</title>
</head>
<body>
<#assign s = services>
<main>
    <div id="container">
    <h1>List of repositories</h1>
    <ul>
    <#list s as service>
	<li><a href="_reconcile/${service}">${service}</a></li>
    </#list>
    </ul>
</main>
</body>
</html>

