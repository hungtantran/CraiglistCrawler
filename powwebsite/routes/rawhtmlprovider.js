var mysql      = require('mysql');
var configs    = require("./config");

var connection = mysql.createConnection({
  host     : configs.dbhost,
  user     : configs.dbuser,
  password : configs.dbpassword
});

RawHTMLProvider = function() {
  console.log("New RawHTMLProvider");
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

// Get raw content with given id only for page identified by human or machine that it's weed page
RawHTMLProvider.prototype.getContent = function(contentId, callback) {
  var query =
    'SELECT * FROM rawhtml WHERE id = ' + contentId + ' AND (positive = 1 OR predict1 = 1 OR predict2 = 1)';

  this.connection.query(query, function(err, rows) {
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
};

exports.RawHTMLProvider = RawHTMLProvider;