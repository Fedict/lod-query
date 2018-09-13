<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<style>
<#include "style.css">
</style>
<title>Reconciliation service</title>
</head>
<body>
<#assign s = service>
<#assign r = repoName>
<main>
    <div id="container">
    <h1>Reconciliation service</h1>
    <table>
	<tr>
	    <th>Parameters</th>
	    <th>Examples</th>
	    <th>Description</th></tr>
	    <#assign params = s.get("param")>
	    <#assign examples = s.get("example")>
	    <tr>
		<td><ul>
		    <#list params  as param><li>${param}</li></#list>
		    </ul>
		</td>
		<td><ul>
		    <#if examples?has_content>
		    <#list examples as ex>
			<li><a href="${r}?${ex}">${ex}</a></li><br/>
		    </#list>
		    </#if>
		    </ul>
		</td>
		<td>${s.description}</td></tr>
    </table>
    </div>
</main>
</body>
</html>