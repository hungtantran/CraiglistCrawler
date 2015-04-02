var mysql      = require('mysql');
var configs    = require("./config");

MySQLConnectionProvider = function() {
};

MySQLConnectionProvider.prototype.getConnection = function() {
  var connection = mysql.createConnection({
    host     : configs.dbhost,
    port     : configs.dbport,
    user     : configs.dbuser,
    password : configs.dbpassword
  });

  // Start the connection
  connection.connect(function(err) {
    if (err) {
      console.error('error connecting: ' + err.stack);
      return;
    }
  });

  // Specify which database to use
  connection.query('USE ' + configs.dbdatabase, function(err, rows) {
    if (err)
      console.error('error use database: ' + err.stack); // 'ER_BAD_DB_ERROR'
      return;
  });

  return connection;
};

exports.MySQLConnectionProvider = MySQLConnectionProvider;