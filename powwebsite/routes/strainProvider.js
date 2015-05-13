var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();
var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();

StrainProvider = function() {
};

// Get all strains post
StrainProvider.prototype.getAllActivePostStrain = function(callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT \
      location_fk AS id, \
      strain_id, \
      strains.type AS type_id \
    FROM \
      posting_types, \
      posting_location, \
      strains \
    WHERE \
      posting_location_id = location_fk AND \
      strains.id = strain_id \
    AND active = 1';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

// Get all strains
StrainProvider.prototype.getAllStrains = function(callback) {
  var connection = connectionProvider.getConnection();

  var query = 'SELECT * FROM strains';

  connection.query(query, function(err, rows) {
    if (err) {
      callback (err);
    } else {
      callback(null, rows);
    }
  });

  connection.end();
};

exports.StrainProvider = StrainProvider;