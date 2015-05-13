<?php
include ("config.php");
include ("imageutils.php");


function getMovieById($id)
{
	$q=mysql_query("SELECT name,hash,year,description,url FROM `movie` where id=".$id)
		or die("Invalid query: " . mysql_error());
	$row=mysql_fetch_row($q);
	$elem['name']=$row[0];
	$elem['hash']=$row[1];
	$elem['year']=$row[2];
	$elem['description']=$row[3];
	$elem['url']=$row[4];
	return $elem;
}

function getRatByMovieId($mov_id)
{
	$q=mysql_query("SELECT name,value,max_value FROM `rating` where mov_id=".$mov_id)
		or die("Invalid query: " . mysql_error());
	$r=array();
	$i=0;
	while($row=mysql_fetch_array($q))
	{
		$r[$i]['name']=$row[0];
		$r[$i]['value']=$row[1];
		$r[$i]['max_value']=$row[2];
	}
	return $r;
}

if (isset($_GET['id'])) 
{
	$row=getMovieById($_GET['id']);
	echo json_encode($row,JSON_UNESCAPED_UNICODE);
	exit;
}
if (isset($_GET['hash']) )
{
	$id=0;
	$min_d=30; //max distance
	$q=mysql_query("select `id`,`hash` FROM `movie`")
		or die("Invalid query: ".mysql_error());
	while($row=mysql_fetch_array($q))
	{
		
		$d=calcHammingDistance($_GET['hash'],$row['hash']);
		if ($d<$min_d) {$min_d=$d; $id=$row['id'];}	
	}
	if ($min_d>6) 
	{
		echo json_decode("0"); exit;
	}
	$elem=getMovieById($id);
	$elem['ratings']=getRatByMovieId($id);
	$elem['description']=$min_d;
	echo json_encode($elem,JSON_UNESCAPED_UNICODE);
	exit;
}
	
?>