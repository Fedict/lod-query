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
<#assign r = repoName>
<main>
    <div id="container">
    <h1>List of queries for ${r}</h1>
    <table>
	<tr><th>Name</th>
	    <th>Parameters</th>
	    <th>Examples</th>
	    <th>Description</th></tr>
	<#list q as file, comment>
	    <#assign name = file?remove_ending(".qr")>
	    <#assign params = comment.get("param")>
	    <#assign examples = comment.get("example")>
	    <tr><td>${name}</td>
		<td><ul>
		    <#list params  as param><li>${param}</li></#list>
		    </ul>
		</td>
		<td><ul>
		    <#if examples?has_content>
		    <#list examples as ex>
			<li><a href="${r}/${name}?${ex}">${ex}</a></li><br/>
		    </#list>
		    <#else>
			<li><a href="${r}/${name}">${name}</a></li>
		    </#if>
		    </ul>
		</td>
		<td>${comment.description}</td></tr>
	</#list>
    </table>
    </div>
</main>
</body>
</html>

