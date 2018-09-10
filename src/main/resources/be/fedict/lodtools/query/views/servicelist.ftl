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
<main>
    <div id="container">
    <h1>List of reconciliation services</h1>
    <ul>
    <#list services as service>
        <li><a href="_reconcile/${service}">${service}</a></li>
    </#list>
    </ul>
</main>
</main>
</body>
</html>

