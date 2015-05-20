// Function that reupdate the display with the newest filter
function updateDisplay(displayInfo, initializeParams) {
  if (displayInfo === undefined) {
    displayInfo = cache;
  }

  var isInitializePosting = true;
  var isInitializeMap = true;
  if (initializeParams !== undefined) {
    isInitializePosting = initializeParams[0];
    isInitializeMap = initializeParams[1];
  }

  if (isInitializeMap === undefined || isInitializeMap) {
    initializeMap(displayInfo['postings']);
  }

  if (isInitializePosting === undefined || isInitializePosting) {
    initializePostings(displayInfo['postings']);
  }
}

// Callback function to handle change in map
function handleMapChange() {
  if(typeof timeoutID === "number") {
    window.clearTimeout(timeoutID);
    delete timeoutID;
  }

  timeoutID = window.setTimeout(function() {
    var newMapBound = map.getBounds();
    // Only update when map bound change
    if (mapBound === null || !newMapBound.equals(mapBound)) {
      mapBound = newMapBound;
      updateDisplay();
    }
  }, 1000);
}

// Function that draw marker on map
function drawMarker(map, markers) {
  // Clear all existing markers first
  for (var i = 0; i < markerArray.length; i++) {
    markerArray[i].setMap(null);
  }

  markerArray.length = 0;

  for (var i = 0; i < markers.length; ++i)
  {
    lat = markers[i]['lat'];
    lng = markers[i]['lng'];

    if (lat != null && lng != null) {
      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, lng),
        map: map
      });

      markerArray.push(marker);
    }
  }
}

// Reinitialize the map, call to redraw the map
function initializeMap(markers, redrawMap) {
  // Initialize maps
  if (map === null || (redrawMap != null && redrawMap === true)) {
    map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
    google.maps.event.addListener(map, 'zoom_changed', handleMapChange);
  }

  // Initialize markers
  if (map !== null) {
    if (markers !== null) {
      drawMarker(map, markers);
    }

    mapBound = map.getBounds();
  }
}

function initializePostings(postings) {
  if (postings === null || postings === undefined) {
    return;
  }

  var table = document.getElementById('table_body_posting');
  for(var i = table.rows.length - 1; i >= 0; --i)
  {
      table.deleteRow(i);
  }

  var totalSalePosts = 0;

  // Chrome, safara and ie has different order or json array
  var startIndex = 0;
  var endIndex = postings.length;
  var step = 1;
  if (postings.length > 1) {
    if (postings[postings.length-1]['datePosted'] < postings[0]['datePosted']) {
      startIndex = postings.length - 1;
      endIndex = -1;
      step = -1;
    }
  }

  for (var i = startIndex; i !== endIndex; i += step)
  {
    if (!postings[i]['city']) {
      continue;
    }

    lat = postings[i]['lat'];
    lng = postings[i]['lng'];

    var postingLocation = new google.maps.LatLng(lat, lng);

    if (mapBound !== undefined && !mapBound.contains(postingLocation)) {
      continue;
    }

    var row = table.insertRow(table.length);
    
    var index = 0;

    // Date/Location Posted cell
    var dateLocationPosted = row.insertCell(index++);
    var url = '/posting/' + postings[i]['url'];
    dateLocationPosted.innerHTML = postings[i]['datePosted'] + " - " + postings[i]['city'];
    dateLocationPosted.setAttribute('class','col-xs-4 col-sm-2 col-md-2 col-lg-2');

    // Title cell
    var title = row.insertCell(index++);
    title.innerHTML = '<strong><a href="' + url + '">' + postings[i]['title'] + '</a></strong>';
    title.setAttribute('class','col-xs-8 col-sm-8 col-md-6 col-lg-6');

    // Code for query with grouping
    // Quantity cell
    var quantity = row.insertCell(index++);
    var quantityString = "";
    if (!postings[i]['quantity']) {
      // quantityString = 'Check Quantity!';
      quantityString = '.........';
    } else {
      var gramArray = postings[i]['quantity'][0];
      if (gramArray) {
        for (var j = 0; j < gramArray.length; ++j) {
          quantityString += gramArray[j] + "g, "
        }
      }

      var ounceArray = postings[i]['quantity'][1];
      if (ounceArray) {
        for (var j = 0; j < ounceArray.length; ++j) {
          quantityString += ounceArray[j] + "oz, "
        }
      }
    }

    quantity.innerHTML = quantityString;
    quantity.setAttribute('class','hidden-xs col-sm-2 col-md-2 col-lg-2');

    // Price cell
    var price = row.insertCell(index++);

    var priceString = "";
    if (!postings[i]['price']) {
      // priceString = 'Check Price!';
      priceString = '.........';
    } else {
      for (var j = 0; j < postings[i]['price'].length; ++j) {
        if (j > 6) {
          priceString += "..."
          break;
        }
        priceString += "$" + postings[i]['price'][j] + ", "
      }
    }

    price.innerHTML = priceString;
    price.setAttribute('class','hidden-xs hidden-sm col-md-2 col-lg-2');

    ++totalSalePosts;
  }

  if (totalSalePosts === 0) {
    var row = table.insertRow(table.length);
    var noPostLink = row.insertCell(0);
    noPostLink.innerHTML = '<a href=\'/state/all\'>No marijuana sales here now. Checkout other sales around the US</a>';
    noPostLink.setAttribute('class','col-xs-12 col-sm-12 col-md-12 col-lg-12');
  }
}

// Function to create a new map
function newMap(latitude, longtitude, zoom, divId, draggable) {
  var draggable_ = typeof draggable ==='undefined' ? true : draggable;

  var mapOptions = {
    center: { lat: latitude, lng: longtitude},
    zoom: zoom,
    draggable: draggable_
  };

  map = new google.maps.Map(document.getElementById(divId),
    mapOptions);

  return map;
}

function newXMLRequest(func, cacheEntry, extraParams) {
  var xmlhttp;

  // code for IE7+, Firefox, Chrome, Opera, Safari
  if (window.XMLHttpRequest)
  {
    xmlhttp=new XMLHttpRequest();
  }
  // code for IE6, IE5
  else
  {
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.onreadystatechange=function()
  {
    if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
      var docs = JSON.parse(xmlhttp.responseText);
      // Store response
      if (cacheEntry !== undefined) {
        cache[cacheEntry] = docs;
      }

      // Invoke callback function
      if (func != null) {
        func(cache, extraParams);
      }
    }
  }

  return xmlhttp;
}

function loadData() {
  geocoder = new google.maps.Geocoder();

  // Postings xml request
  var params = location.pathname.split('/');
  var postUrl = "/strain/";

  postUrl += params[2];

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'postings',
    [true, true]);
  xmlhttpPostings.open("POST", postUrl, true);
  xmlhttpPostings.send();
}