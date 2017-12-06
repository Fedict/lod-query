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
    <h1>List of queries</h1>
    <table>
	<tr><th>Name</th><th>Parameters</th><th>Description</th></tr>
	<#list q as file, comment>
	    <#assign name = file?remove_ending(".qr")>
	    <#assign params = comment.get("param")>
	    <#assign examples = comment.get("example")>
	    <tr><td>${name}</td>
		<td><#list params  as param>${param}</#list></td>
		<td>${comment.description}
		    <#if examples?has_content>
		    Examples:
		    <ul>
		    <#list examples as ex>
			<li><a href="${r}/${name}?${ex}">${ex}</a></li><br/>
		    </#list>
		    </ul>
		    </#if>
		</td></tr>
	</#list>
    </table>
    </div>
</main>
</body>
</html>

