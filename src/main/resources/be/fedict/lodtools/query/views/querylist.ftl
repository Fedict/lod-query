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
	<#list q as name, desc>
	<tr><th>${name?remove_ending(".qr")}</th>
	    <td><#list desc?split("#") as p>${p}<br/></#list></td></tr>
	</#list>
    </section>
    </div>
</main>
</body>
</html>

