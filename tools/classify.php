<?php
require_once("DBConnection.php");
require_once("database_config.php");

$order = 1;
if (isset($_GET["order"])) {
    $order = $_GET["order"];
}

$confirmation = "null";
if (isset($_GET["confirmation"])) {
    $confirmation = $_GET["confirmation"];
}
?>

<form method = "GET" action = "classify.php">
    Is weed page?<input type="submit" name="confirmation" value="yes" /><br/>
    Is weed page?<input type="submit" name="confirmation" value="no" />
    <input type="hidden" name="order" value ="<?php echo $order ?>"/>
</form><br/>

<?php
$RawHTMLDB = new RawHTMLDB();

//Provide config setting
echo "before";
$RawHTMLDB->SetConfig($default_hostname, $default_username, $default_password, $default_db);
echo "after";

if ($confirmation == "null") {
    $result = $RawHTMLDB->getOrder($order);
    echo sizeof($result);
    if (sizeof($result) == 1) {
        $result = $result[0];

        echo "Link = ".$result["url"]."<br/>";
        echo "Location = ".$result["country"].", ".$result["state"].", ".$result["city"]."<br/>";
        echo "Positive = ".$result["positive"]."<br/>";
        echo "Predict1 = ".$result["predict1"]."<br/>";
        echo "Predict2 = ".$result["predict2"]."<br/>";
        echo "Quantities = ".$result["alt_quantities"]."<br/>";
        echo "Prices = ".$result["alt_prices"]."<br/>";
        echo "Html = ".$result["html"]."<br/>";
    } else {
        echo "Out of html<br/>";
        echo "<a href='/classify.php?confirmation=null&order=".($order+1)."'>Next</a>";
    }
}
else
{
    if ($confirmation == "yes") {
        $RawHTMLDB->updatePositive($order, 1);
    } else if ($confirmation == "no") {
        $RawHTMLDB->updatePositive($order, 0);
    }

    echo "<script>";
    echo "window.location = '/classify.php?confirmation=null&order=".($order+1)."'";
    echo "</script>";
}
?>
