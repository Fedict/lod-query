<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<style>
<#include "style.css">
</style>
<title>List of queries</title>
</head>
<body>
<#assign q = queries>
<main>
    <div id="container">
    <h1>List of queries</h1>
    <table>
	<tr><th>Name</th><th>Parameters</th><th>Description</th></tr>
	<#list q as name, str>
	    <#assign params = []>
	    <#assign desc = "">
	    <#list str?split("#") as p>
		<#if p?starts_with(" @param ")>
		    <#assign params = params + [p?keep_after(" @param ")]>
		<#else>
		    <#assign desc = desc + p>
		</#if>
	    </#list>
	    <tr><td>${name?remove_ending(".qr")}</td>
		<td><#list params as param>${param}</#list></td>
		<td>${desc}</td></tr>
	</#list>
    </section>
    </div>
</main>
</body>
</html>

