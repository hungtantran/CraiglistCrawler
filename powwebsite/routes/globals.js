var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();

var locations = null;
postingLocationProvider.getLocations(function(error, docs) {
    if (error != null) {
        console.error('error to get locations: ' + error.stack);
        process.exit(1);
    }

    locations = docs;
    exports.locations = locations;
})

exports.postingLocationProvider = postingLocationProvider;