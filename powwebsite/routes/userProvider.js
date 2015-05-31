var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

UserProvider = function() {
};

// Insert new user
UserProvider.prototype.insertUser = function(email, username, password, callback) {
  var connection = connectionProvider.getConnection();

  var insertQuery = 'INSERT INTO users (email, username, password) VALUES (?, ?, ?)';

  var insertUser = connection.query(insertQuery,
    [email, username, password],
    function(err, rows) {
      if (err) {
        console.log('Request to create user failed ' + err);
        callback(err);
      } else {
        callback(null, rows);
      }
  });

  connection.end();
};

// Find user
UserProvider.prototype.getUser = function(username, password, callback) {
  var connection = connectionProvider.getConnection();

  var userQuery = 'SELECT * FROM users WHERE username = ? AND password = ?';

  connection.query(userQuery,
    [username, password],
    function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.UserProvider = UserProvider;