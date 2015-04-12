var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req, res){
    res.json(globals.prices);
});

// Get some state json
router.post('/:param', function(req, res){
    var params = req.params.param.split("-");
    var id = params[params.length-1];

    var info = [];

    // Posting page
    if (!isNaN(parseInt(id))) {
        var city = null;

        for (var i = 0; i < globals.postingLocations.length; ++i) {
            if (globals.postingLocations[i]['id'] == id) {
                city = globals.postingLocations[i]['city'];
                break;
            }
        }

        for (var i = 0; i < globals.prices.length; ++i) {
            if (globals.prices[i]['city'].toUpperCase() === city.toUpperCase()) {
                info.push(globals.prices[i]);
            }
        }
    }
    // State page
    else {
        var state = req.params.param;

        for (var i = 0; i < globals.prices.length; ++i) {
            if (globals.prices[i]['state'].toUpperCase() === state.toUpperCase()) {
                info.push(globals.prices[i]);
            }
        }
    }

    res.json(info);
});

module.exports = router;
