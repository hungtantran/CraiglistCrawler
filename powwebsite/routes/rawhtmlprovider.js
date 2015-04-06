var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

RawHTMLProvider = function() {
};

// Get raw content with given id only for page identified by human or machine that it's weed page
RawHTMLProvider.prototype.getContent = function(contentId, callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT * FROM posting_location LEFT JOIN (select rawhtml.id, rawhtml.url from rawhtml) AS query ON location_fk=query.id WHERE location_fk = ' + contentId;

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

exports.RawHTMLProvider = RawHTMLProvider;