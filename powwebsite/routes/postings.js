var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req, res){
    res.json(globals.postings);
});

// Get some state json
router.post('/:param', function(req, res){
    var params = req.params.param.split("-");
    var id = params[params.length-1];
    
    var state = null;
    if (!isNaN(parseInt(id))) {
        for (var i = 0; i < globals.postingStates.length; ++i) {
            if (globals.postingStates[i]['id'] == id) {
                state = globals.postingStates[i]['state'];
                break;
            }
        }
    } else {
        state = req.params.param;
    }

    stateInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
            stateInfo.push(globals.postings[i]);
        }
    }

    res.json(stateInfo);
});

module.exports = router;
