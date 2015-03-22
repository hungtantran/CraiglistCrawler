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

// Get all prices
PricesProvider.prototype.getAllPrices = function(callback) {
  var query =
    'SELECT \
    `prices`.`price_id` AS `price_id`, \
    `prices`.`price_fk` AS `price_fk`, \
    `prices`.`price` AS `price`, \
    `prices`.`quantity` AS `quantity`, \
    `prices`.`unit` AS `unit`, \
    `prices`.`human_generated` AS `human_generated`, \
    `posting_location`.`state` AS `state`, \
    `posting_location`.`city` AS `city`, \
    `posting_location`.`latitude` AS `latitude`, \
    `posting_location`.`longitude` AS `longitude`, \
    `posting_location`.`location_fk` AS `location_fk` \
  FROM \
    `prices` \
  INNER JOIN `posting_location` ON ( \
    `prices`.`price_fk` = `posting_location`.`location_fk` \
  )';

  this.connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });
};

PricesProvider.prototype.getPrices = function(postingId, callback) {
  var query =
    'SELECT \
    `prices`.`price_id` AS `price_id`, \
    `prices`.`price_fk` AS `price_fk`, \
    `prices`.`price` AS `price`, \
    `prices`.`quantity` AS `quantity`, \
    `prices`.`unit` AS `unit`, \
    `prices`.`human_generated` AS `human_generated`, \
    `posting_location`.`state` AS `state`, \
    `posting_location`.`city` AS `city`, \
    `posting_location`.`latitude` AS `latitude`, \
    `posting_location`.`longitude` AS `longitude`, \
    `posting_location`.`location_fk` AS `location_fk` \
  FROM \
    `prices` \
  INNER JOIN `posting_location` ON ( \
    `prices`.`price_fk` = `posting_location`.`location_fk`) \
  WHERE `prices`.`price_fk` = ' + postingId;
}

// Get all posting
PricesProvider.prototype.getPostings = function(callback) {
  var query =
    'SELECT * \
FROM \
  ( \
    ( \
      SELECT \
        location_fk AS id, \
        price, \
        quantity, \
        unit, \
        A.state, \
        A.city, \
        A.latitude AS lat1, \
        A.longitude AS lng1, \
        C.latitude AS lat2, \
        C.longitude AS lng2, \
        datePosted \
      FROM \
        posting_location AS A, \
        prices AS B, \
        location_link AS C \
      WHERE \
        price_fk = location_fk \
      AND location_link_fk = C.id \
    ) \
    UNION \
      ( \
        SELECT \
          location_fk AS id, \
          NULL AS price, \
          NULL AS quantity, \
          NULL AS unit, \
          A.state, \
          A.city, \
          A.latitude AS lat1, \
          A.longitude AS lng1, \
          C.latitude AS lat2, \
          C.longitude AS lng2, \
          datePosted \
        FROM \
          posting_location AS A, \
          location_link AS C \
        WHERE \
          location_link_fk = C.id \
        AND location_fk NOT IN (SELECT price_fk FROM prices) \
      ) \
  ) AS D \
ORDER BY \
  datePosted DESC';

  this.connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });
};

exports.PricesProvider = PricesProvider;