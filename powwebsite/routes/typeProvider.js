var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();
var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();

TypeProvider = function() {
};

// Get all prices
TypeProvider.prototype.getAllTypes = function(callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT * FROM types';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.TypeProvider = TypeProvider;