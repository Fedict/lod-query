<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<style>
<#include "style.css">
</style>
</head>
<body>
<#assign l = labels>
<main>
    <#list l as label>
	<p>${label}</p>
    </#list>
</main>
</body>
</html>

