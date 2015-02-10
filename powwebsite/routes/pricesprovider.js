var mysql      = require('mysql');
var configs    = require("./config");

var connection = mysql.createConnection({
  host     : configs.dbhost,
  user     : configs.dbuser,
  password : configs.dbpassword
});

PricesProvider = function() {
  console.log("New PricesProvider");
  this.connection = mysql.createConnection({
    host     : configs.dbhost,
    port     : configs.dbport,
    user     : configs.dbuser,
    password : configs.dbpassword
  });

  // Start the connection
  this.connection.connect(function(err) {
    if (err) {
      console.error('error connecting: ' + err.stack);
      return;
    }
  });

  // Specify which database to use
  this.connection.query('USE ' + configs.dbdatabase, function(err, rows) {
    if (err)
      console.error('error use database: ' + err.stack); // 'ER_BAD_DB_ERROR'
      return;
  });
};

// Find the size of article_table
PricesProvider.prototype.getPrices = function(callback) {
  query  = 'SELECT `prices`.`price_id` AS `price_id`, `prices`.`price_fk` AS `price_fk`, `prices`.`price` AS `price`, `prices`.`quantity` AS `quantity`, `prices`.`unit` AS `unit`, `prices`.`human_generated` AS `human_generated`, `posting_location`.`state` AS `state`, `posting_location`.`city` AS `city`, `posting_location`.`latitude` AS `latitude`, `posting_location`.`longitude` AS `longitude`, `posting_location`.`location_fk` AS `location_fk` FROM `prices` INNER JOIN `posting_location` ON (`prices`.`price_fk` = `posting_location`.`location_fk`)'

  this.connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });
};

exports.PricesProvider = PricesProvider;