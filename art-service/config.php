<?php
//header('Content-Type: text/html; charset=utf-8');
header('Content-Type: application/json; charset=utf-8');
$host = "localhost:3306";
$user = "root";
$pw = "";
$db = "art_movies_db";
if (!mysql_connect($host, $user, $pw))
{
	echo "mysql error!";
	exit;
}
mysql_select_db($db) or die("Invalid query: " . mysql_error());
mysql_query("set names utf8");
?>