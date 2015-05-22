var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

NewsProvider = function() {
};

// Get all prices
NewsProvider.prototype.getAllNews = function(callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT * FROM news ORDER BY datePosted DESC';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.NewsProvider = NewsProvider;