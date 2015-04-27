var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req, res){
    res.json(globals.localBusinesses);
});

// Get some location json
router.post('/:param', function(req, res){
    var params = req.params.param.split("-");
    var id = params[params.length-1];
    
    var info = [];

    // Local business page
    if (!isNaN(parseInt(id))) {
        var city = null;

        for (var i = 0; i < globals.localBusinesses.length; ++i) {
            if (globals.localBusinesses[i]['id'] == id) {
                city = globals.localBusinesses[i]['city'];
                break;
            }
        }

        for (var i = 0; i < globals.localBusinesses.length; ++i) {
            if (globals.localBusinesses[i]['city'].toUpperCase() === city.toUpperCase()) {
                info.push(globals.localBusinesses[i]);
            }
        }
    }
    // State page
    else {
        var state = req.params.param;

        for (var i = 0; i < globals.localBusinesses.length; ++i) {
            if (globals.localBusinesses[i]['state'].toUpperCase() === state.toUpperCase()) {
                info.push(globals.localBusinesses[i]);
            }
        }
    }

    res.json(info);
});

module.exports = router;
