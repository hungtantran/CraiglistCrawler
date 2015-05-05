var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

LocalBusinessProvider = function() {
};

// Get all prices
LocalBusinessProvider.prototype.getAllLocalBusinesses = function(callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT state, city, address, phone_number, rating, latitude as lat, longitude as lng, rawhtml_fk as id, title, url FROM `local_business` WHERE title IS NOT NULL AND rating > 3 ORDER BY rating DESC';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.LocalBusinessProvider = LocalBusinessProvider;