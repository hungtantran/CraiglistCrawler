var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

PostingLocationProvider = function() {
};

PostingLocationProvider.prototype.getLocations = function(callback) {
  var connection = connectionProvider.getConnection();

  connection.query('SELECT location_fk as id, state, city, latitude, longitude FROM posting_location', function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

PostingLocationProvider.prototype.getResults = function(start, numResults, callback) {
  var connection = connectionProvider.getConnection();

  var queryWithPriceGrouping =
  'SELECT * \
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
        datePosted IS NOT NULL \
    ) AS D \
  ORDER BY \
  datePosted DESC, quantity DESC \
  LIMIT ' + start + ', ' + numResults;

  connection.query(queryWithPriceGrouping, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.PostingLocationProvider = PostingLocationProvider;

