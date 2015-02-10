var mysql      = require('mysql');
var configs    = require("./config");

var connection = mysql.createConnection({
  host     : configs.dbhost,
  user     : configs.dbuser,
  password : configs.dbpassword
});

PostingLocationProvider = function() {
  console.log("New PostingLocationProvider");
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

PostingLocationProvider.prototype.getLocations = function(callback) {
  this.connection.query('SELECT location_fk as id, state, city, latitude, longitude FROM posting_location', function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });
};

exports.PostingLocationProvider = PostingLocationProvider;

