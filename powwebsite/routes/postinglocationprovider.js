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

exports.PostingLocationProvider = PostingLocationProvider;

