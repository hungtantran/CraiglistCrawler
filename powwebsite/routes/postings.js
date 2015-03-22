var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req, res){
    var day = new Date();
    day.setDate(day.getDate() - 60);
    var date = day.getDate();
    var month = day.getMonth() + 1; //January is 0!
    var year = day.getFullYear();
    var dateString = '' + year + '-' + month + '-' + date;

    res.json(globals.postings);
});

// Get some state json
router.post('/:state', function(req, res){
    var state = req.params.state

    stateInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
            stateInfo.push(globals.postings[i]);
        }
    }

    res.json(stateInfo);
});

module.exports = router;
