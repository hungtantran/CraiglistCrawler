<?php
class rawhtmlDB {
	var $hostname;
	var $username;
	var $password;
	var $db;
	var $dbcon;
	
	function SetConfig($host, $user, $pass, $db)
	{
		$this->hostname = $host;
		$this->username = $user;
		$this->password = $pass;
		$this->db = $db;
		$db = new PDO("mysql:host=".$this->hostname.";port=4200;dbname=".$this->db, $this->username, $this->password);
		$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);		
		$this->dbcon = $db;
	}
	
	/* Function returns the list of rawhtml */
	public function get() {
		/* Prepare database */
		$db = $this->dbcon;	

		/* Select all rawhtml */
		$sql = "SELECT * FROM rawhtml";
		$sql = $db->prepare($sql);
		$sql->execute();

		/* Return the array of results */
		return $sql->fetchAll(PDO::FETCH_ASSOC); 
	}

	/* Function returns the list of rawhtml */
	public function getOrder($order) {
		/* Prepare database */
		$db = $this->dbcon;	

		/* Select all rawhtml */
		$sql = "SELECT * FROM rawhtml WHERE id = ".$order;
		$sql = $db->prepare($sql);
		$sql->execute();

		/* Return the array of results */
		return $sql->fetchAll(PDO::FETCH_ASSOC); 
	}

	public function getAltPricesQuantitiesOrder($order) {
		/* Prepare database */
		$db = $this->dbcon;	

		/* Select all rawhtml */
		$sql = "SELECT * FROM rawhtml WHERE alt_quantities IS NOT NULL AND alt_prices IS NOT NULL LIMIT ".($order-1).", 1";
		$sql = $db->prepare($sql);
		$sql->execute();

		/* Return the array of results */
		return $sql->fetchAll(PDO::FETCH_ASSOC); 
	}

	public function updatePositive($order, $positive)
	{
		$db = $this->dbcon;

		/* Clear all the has updated price flag*/
		$sql = "UPDATE rawhtml SET positive = ".$positive." WHERE id = ".$order;
		echo $sql."<br/>";
		$sql = $db->prepare($sql);
		$sql->execute();
	}
}

class PricesDB {
	var $hostname;
	var $username;
	var $password;
	var $db;
	var $dbcon;
	
	function SetConfig($host, $user, $pass, $db)
	{
		$this->hostname = $host;
		$this->username = $user;
		$this->password = $pass;
		$this->db = $db;
		$db = new PDO("mysql:host=".$this->hostname.";dbname=".$this->db, $this->username, $this->password);
		$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);		
		$this->dbcon = $db;
	}
	
	public function add ($price_fk, $price, $quantity, $unit){
		$db = $this->dbcon;

		$sql = "INSERT INTO prices (price_fk, price, quantity, unit) VALUES ($price_fk, $price, $quantity, '$unit')";
		$sql = $db->prepare($sql);
		$sql->execute();
	}
}
?>
