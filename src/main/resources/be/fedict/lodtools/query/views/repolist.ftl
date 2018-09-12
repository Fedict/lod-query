<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<style>
<#include "style.css">
</style>
<title>List of repositories</title>
</head>
<body>
<#assign r = repositories>
<main>
    <div id="container">
    <h1>List of repositories</h1>
    <ul>
    <#list r as repo>
	<li><a href="${path}/${repo}">${repo}</a></li>
    </#list>
    </ul>
</main>
</body>
</html>

