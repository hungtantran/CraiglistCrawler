var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req, res){
    res.json(globals.postings);
});

// Get some location json
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

        for (var i = 0; i < globals.postings.length; ++i) {
            if (globals.postings[i]['city'].toUpperCase() === city.toUpperCase()) {
                info.push(globals.postings[i]);
            }
        }
    }
    // State page
    else {
        var state = req.params.param;

        for (var i = 0; i < globals.postings.length; ++i) {
            if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
                info.push(globals.postings[i]);
            }
        }
    }

    res.json(info);
});

module.exports = router;
