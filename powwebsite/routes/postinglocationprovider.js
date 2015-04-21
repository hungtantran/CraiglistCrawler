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
        A.alt_prices AS price, \
        A.alt_quantities AS quantity, \
        A.latitude AS lat1, \
        A.longitude AS lng1, \
        C.latitude AS lat2, \
        C.longitude AS lng2, \
        datePosted, \
        title, \
        duplicatePostId \
      FROM \
        posting_location AS A, \
        location_link AS C \
      WHERE \
        location_link_fk = C.id AND \
        datePosted IS NOT NULL AND \
        duplicatePostId IS NULL \
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

// Get raw content with given id only for page identified by human or machine that it's weed page
PostingLocationProvider.prototype.getContent = function(contentId, callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT * FROM posting_location WHERE location_fk = ' + contentId;

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      var content = null;

      if (rows.length >= 0) {
        content = rows[0];
      }
      
      callback(null, content);
    }
  });

  connection.end();
};

exports.PostingLocationProvider = PostingLocationProvider;

