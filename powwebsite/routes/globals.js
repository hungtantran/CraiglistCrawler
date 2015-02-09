var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();

var locations = null;
rawHTMLProvider.getLocations(function(error, docs) {
    if (error != null) {
        console.error('error to get locations: ' + error.stack);
        process.exit(1);
    }

    locations = docs;
    exports.locations = locations;
})

exports.rawHTMLProvider = rawHTMLProvider;