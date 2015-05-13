<?php
function calcHammingDistance($hash1,$hash2)
{
	//if (strlen($hash1)!=strlen($hash2)) return -1;
    //convert hex to bits
	$hb1=hex2bin($hash1);
	$hb2=hex2bin($hash2);
	while (strlen($hb1)<64) $hb1="0".$hb1;
	while (strlen($hb2)<64) $hb2="0".$hb2;
	if (strlen($hb1)!=strlen($hb2)) return -1;
	$d=0;
	for ($i=0;$i<strlen($hb1);$i++)
	{
		if ($hb1[$i]!=$hb2[$i]) $d++;
	}
	return $d;
}

?>