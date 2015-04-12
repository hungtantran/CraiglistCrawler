var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();
var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();

var maxPostTimeValidInDays = 7;
var maxPricesTimeValidInDays = 31;

PricesProvider = function() {
};

// Get all prices
PricesProvider.prototype.getAllPostingLocations = function(callback) {
  var connection = connectionProvider.getConnection();

  var d = new Date();
  d.setDate(d.getDate() - maxPricesTimeValidInDays);
  var dateString = commonHelper.FormatDate(d);

  var query =
    'SELECT \
    `posting_location`.`state` AS `state`, \
    `posting_location`.`city` AS `city`, \
    `posting_location`.`location_fk` AS `id` \
  FROM `posting_location` \
  WHERE datePosted IS NOT NULL';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

// Get all prices
PricesProvider.prototype.getAllPrices = function(callback) {
  var connection = connectionProvider.getConnection();

  var d = new Date();
  d.setDate(d.getDate() - maxPricesTimeValidInDays);
  var dateString = commonHelper.FormatDate(d);

  var query =
    'SELECT \
    `prices`.`price_fk` AS `id`, \
    `prices`.`price` AS `price`, \
    `prices`.`quantity` AS `quantity`, \
    `prices`.`unit` AS `unit`, \
    `posting_location`.`state` AS `state`, \
    `posting_location`.`city` AS `city`, \
    `posting_location`.`latitude` AS `lat`, \
    `posting_location`.`longitude` AS `lng` \
  FROM \
    `prices` \
  INNER JOIN `posting_location` ON ( \
    `prices`.`price_fk` = `posting_location`.`location_fk`) \
  WHERE datePosted IS NOT NULL AND datePosted >= "' + dateString + '"';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

// Get all posting
PricesProvider.prototype.getPostings = function(callback) {
  var connection = connectionProvider.getConnection();

  var d = new Date();
  d.setDate(d.getDate() - maxPostTimeValidInDays);
  var dateString = commonHelper.FormatDate(d);
  console.log(dateString);

  var queryWithoutPriceGrouping =
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
        datePosted, \
        title \
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
          datePosted, \
          title \
        FROM \
          posting_location AS A, \
          location_link AS C \
        WHERE \
          location_link_fk = C.id \
        AND location_fk NOT IN (SELECT price_fk FROM prices) \
      ) \
  ) AS D \
  WHERE datePosted IS NOT NULL AND datePosted >= "' + dateString + '" \
  ORDER BY \
  datePosted DESC';

  var queryWithPriceGroupingNotNullQuantity =
    'SELECT *  \
FROM \
    ( \
      SELECT \
        location_fk AS id, \
        A.state, \
        A.city, \
        B.alt_prices AS price, \
        B.alt_quantities AS quantity, \
        A.latitude AS lat1, \
        A.longitude AS lng1, \
        C.latitude AS lat2, \
        C.longitude AS lng2, \
        datePosted, \
        title \
      FROM \
        posting_location AS A, \
        rawhtml AS B, \
        location_link AS C \
      WHERE \
        location_fk = B.id AND \
        location_link_fk = C.id AND \
        datePosted IS NOT NULL AND alt_quantities IS NOT NULL AND \
        datePosted >= "' + dateString + '" \
    ) AS D \
  ORDER BY \
  datePosted DESC';

  var queryWithPriceGroupingNullQuantity =
    'SELECT *  \
FROM \
    ( \
      SELECT \
        location_fk AS id, \
        A.state, \
        A.city, \
        B.alt_prices AS price, \
        B.alt_quantities AS quantity, \
        A.latitude AS lat1, \
        A.longitude AS lng1, \
        C.latitude AS lat2, \
        C.longitude AS lng2, \
        datePosted, \
        title \
      FROM \
        posting_location AS A, \
        rawhtml AS B, \
        location_link AS C \
      WHERE \
        location_fk = B.id AND alt_quantities IS NULL AND \
        location_link_fk = C.id AND datePosted IS NOT NULL AND \
        datePosted >= "' + dateString + '" \
    ) AS D \
  ORDER BY \
  datePosted DESC';

  var queryWithPriceGrouping = 
  'SELECT *  \
FROM \
    ( \
      SELECT \
        location_fk AS id, \
        A.state, \
        A.city, \
        B.alt_prices AS price, \
        B.alt_quantities AS quantity, \
        A.latitude AS lat1, \
        A.longitude AS lng1, \
        C.latitude AS lat2, \
        C.longitude AS lng2, \
        datePosted, \
        title, \
        duplicatePostId \
      FROM \
        posting_location AS A, \
        rawhtml AS B, \
        location_link AS C \
      WHERE \
        location_fk = B.id AND \
        location_link_fk = C.id AND \
        datePosted IS NOT NULL AND \
        datePosted >= "' + dateString + '" \
    ) AS D \
  ORDER BY \
  datePosted DESC, quantity DESC';

  connection.query(queryWithPriceGrouping, function(err, rows) {
    if (err) {
      callback (err);
      connection.end();
    } else {
      callback(null, rows);
      connection.end();
    }
  });
};

exports.PricesProvider = PricesProvider;