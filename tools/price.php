<?php
require_once("DBConnection.php");
require_once("database_config.php");

$order = 1;
if (isset($_GET["order"])) {
    $order = $_GET["order"];
}

if (isset($_POST["order"])) {
    $order = $_POST["order"];
}

$quantity = null;
if (isset($_POST["quantity"])) {
    $quantity = $_POST["quantity"];
}

$unit = null;
if (isset($_POST["unit"])) {
    $unit = $_POST["unit"];
}

$price = null;
if (isset($_POST["price"])) {
    $price = $_POST["price"];
}
?>

<form method = "POST" action = "price.php">
    Quantity:<input type="text" name="quantity" value="" /><br/>
    Unit:<input type="text" name="unit" value="" /><br/>
    Price:<input type="text" name="price" value="" /><br/>
    <input type="hidden" name="order" value ="<?php echo $order ?>"/><br/>
    <input type="submit" value="Submit"/>
</form><br/>

<?php
echo "<a href='/price.php?order=".($order+1)."'>Next</a><br/>";

$RawHTMLDB = new RawHTMLDB();
$PricesDB = new PricesDB();

//Provide config setting
$PricesDB->SetConfig($default_hostname, $default_username, $default_password, $default_db);
$RawHTMLDB->SetConfig($default_hostname, $default_username, $default_password, $default_db);

$result = $RawHTMLDB->getAltPricesQuantitiesOrder($order);

if (sizeof($result) == 1) {
    $result = $result[0];

    echo "Link = ".$result["url"]."<br/>";
    echo "Location = ".$result["country"].", ".$result["state"].", ".$result["city"]."<br/>";
    echo "Positive = ".$result["positive"]."<br/>";
    echo "Predict1 = ".$result["predict1"]."<br/>";
    echo "Predict2 = ".$result["predict2"]."<br/>";
    echo "Quantities = ".$result["alt_quantities"]."<br/>";
    echo "Prices = ".$result["alt_prices"]."<br/>";
    echo $result["html"];
} else {
    echo "Out of html<br/>";
}

if ($price != null && $quantity != null && $unit != null) {
    $PricesDB->add($result["id"], $price, $quantity, $unit);

    echo "<script>";
    echo "window.location = '/price.php?order=".$order."'";
    echo "</script>";
}
?>