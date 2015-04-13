// Function that reupdate the display with the newest filter
function updateDisplay(displayInfo, initializeParams) {
  if (displayInfo === undefined) {
    displayInfo = cache;
  }

  var isInitializePosting = true;
  var isInitializeMap = true;
  var isInitializePrice = true;
  if (initializeParams !== undefined) {
    isInitializePosting = initializeParams[0];
    isInitializeMap = initializeParams[1];
    isInitializePrice = initializeParams[2];
  }

  if (isInitializePosting === undefined ||  isInitializePosting) {
    initializePostings(displayInfo['postings']);
  }

  if (isInitializeMap === undefined || isInitializeMap) {
    initializeMap(displayInfo['postings']);
  }

  if (isInitializePrice === undefined || isInitializePrice) {
    initializePrices(displayInfo['prices']);
  }
}

function initializePrices(prices) {
  $('#price_bin_dist_by_state').empty();
  $('#svgAxis').remove();

  var params = document.URL.split("/");
  var id = params[params.length-1];
  var postingPrices = [];
  var isPostingPage = false;
  if (!isNaN(parseInt(id))) isPostingPage = true;

  newPrices = [];
  for (var i = 0; i < prices.length; ++i) {
    if (isPostingPage && prices[i]['price_fk'] === id) {
      postingPrices.push(prices[i]);
    }

    lat = prices[i]['lat'];
    lng = prices[i]['lng'];

    var priceLocation = new google.maps.LatLng(lat, lng)
    if (mapBound === null || mapBound === undefined || mapBound.contains(priceLocation)) {
      newPrices.push(prices[i]);
    }
  }

  newPriceBin('price_bin_dist_by_state', newPrices);
}

function newPriceBin(divId, prices) {
  var binNames = ['0-5', '5-10', '10-15', '15-20', '20-25', '25-30', '30-35', '35-40','40-45','45+'];
  data = {}
  for (var i=0; i<prices.length; ++i)
  {
    var entry = prices[i];
    var state = entry['state']
    var price = entry['price']
    var unit = entry['unit'];
    var quantityGrams = entry['quantity'];
    // convet ounces to grams
    if (unit=='oz') { quantityGrams = quantityGrams * 28.3495; }
    var pricePerGram = price / quantityGrams;

    // find appropriate state bin to increment
    if (!(state in data)) {
      data[state] = {};
      binNames.map( function(name) { data[state][name] = 0 });
    }

    // increment that bin
    bins = data[state];
    var bin = Math.floor(pricePerGram / 5);
    if (bin > 9) { bin = 9; }
    
    var binKey = binNames[bin]
    bins[binKey] = bins[binKey] + 1;
  }

  arr = [];
  for (k in data) {
    data[k]['state'] = k;
    data[k]['bins'] = binNames.map( function(name) { return {name:name, value:data[k][name], state:k} });

    arr.push(data[k]);
  }

  var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = $('#' + divId).width()- margin.left - margin.right,
    height = 250*arr.length;//250 - margin.top - margin.bottom;
  
  var y0 = d3.scale.ordinal()
    .rangeRoundBands([0, height], .1);

  var y1 = d3.scale.ordinal();

  var x = d3.scale.linear()
      .range([0, width]);

  var color = d3.scale.ordinal()
      .range(["#A2c295","#88b57a","#6DA964","#4C9D51","#3B9748","#319412","#29803A","#217B37","#25803A","#1D7634","#187131","#136C2F","#0E672C","#09622A"]);

  var xAxis = d3.svg.axis()
      .scale(x)
      .orient("top")
      .tickSize(0);

  var yAxis = d3.svg.axis()
      .scale(y0)
      .orient("left");

  var yAxisBins = d3.svg.axis()
      .scale(y1)
      .orient("left")
      .tickSize(0);

  var tooltip = d3.select('#price_bin')
    .append('div')
    .attr('class', 'tooltip');

  tooltip.append('div')
    .attr('class', 'state');
  tooltip.append('div')
    .attr('class', 'label');
  tooltip.append('div')
    .attr('class', 'count');

  var svgAxis = d3.select('#' + 'price_bin').insert("svg", ":first-child")
      .attr("width", "100%")
      .attr("height", 18)
      .attr("id", "svgAxis")
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  var svg = d3.select('#' + divId).append("svg")
      .attr("width", "100%")
      .attr("height", height + margin.top + margin.bottom)
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  y0.domain(Object.keys(data));
  y1.domain(binNames).rangeRoundBands([0, y0.rangeBand()]);
  x.domain([0, d3.max(arr, function(d) { return d3.max(d['bins'], function(d) { return d.value; }); })]);

  svgAxis.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(100,0)")
    .style("position", "fixed")
    .call(xAxis);

  svg.append("g")
      .attr("class", "y axis")
      .attr("transform", "translate("+50+",0)")
      .call(yAxis);

  var state = svg.selectAll(".state")
    .data(arr)
      .enter().append("g")
        .attr("class", "g")
        .attr("transform", function(d) { return "translate(100," + y0(d.state) + ")"; });

  state.append("g")
    .attr("class", "y axis")
    .attr("transform", "translate(0,0)")
    .call(yAxisBins);

  state.selectAll("rect")
    .data(function(d) {
      return d['bins']; })
  .enter().append("text")
    .text(function(d) { 
      if (d.value === null) {
        return 0;
      } else {
        if (d.value === 0) return "";
        return d.value;
      }
    })
    .attr("x", function(d) {
      if (d.value === null) {
        return 0;
      } else {
        return x(d.value) + 4;
      }
    })
    .attr("y", function(d) {
      return y1(d.name) + 4 + y1.rangeBand()/2;
    });
    

  state.selectAll("rect")
      .data(function(d) {
        return d['bins']; })
    .enter().append("rect")
      .attr("width", function(d) { 
        if (d.value === null) {
          return 0;
        } else {
          return x(d.value);
        }
      })
      .attr("x", 0)
      .attr("y", function(d) {
        return y1(d.name)+2;
      })
      .attr("height", y1.rangeBand()-4)
      .on('mouseover', function(d) {
        var xPosition = parseInt(d3.select(this).attr("x") );
        var yPosition = parseInt(d3.select(this).attr("y") );

        tooltip.select(".state").html(d.state);
        tooltip.select(".label").html("Price Range: " + d.name);
        tooltip.select('.count').html("Number of postings:" + d.value);
        tooltip.style('left', xPosition + "px");
        tooltip.style('top', yPosition + "px");
        tooltip.style('display', 'block');
        })
      .on('mouseout', function(d) {
        tooltip.style('display', 'none');
        })
      .on('click', function(d) {
        if (stateFilter === d.state) stateFilter = null;
        else stateFilter = d.state;
        updateDisplay();
        })
      .style("fill", function(d) {
        if (stateFilter === null || d.state === stateFilter) {
          return color(d.name);
        } else {
          return "#F5F5F5";
        }
        });
}

// Callback function to handle change in map
function handleMapChange() {
  mapBound = map.getBounds();

  updateDisplay();
}

// Function that draw marker on map
function drawMarker(map, markers) {
  if (markerClusterer != null) {
    markerClusterer.clearMarkers();
  }

  // Clear all existing markers first
  for (var i = 0; i < markerArray.length; i++) {
    markerArray[i].setMap(null);
  }

  markerArray.length = 0;

  for (var i = 0; i < markers.length; ++i)
  {
    if (stateFilter != null && markers[i]['state'] != stateFilter) {
      continue;
    }

    lat = markers[i]['lat'];
    lng = markers[i]['lng'];

    if (lat != null && lng != null) {
      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, lng),
        map: map
      });

      google.maps.event.addListener(marker, 'click', handleMapChange);

      markerArray.push(marker);
    }
  }

  markerClusterer = new MarkerClusterer(map, markerArray);
}

// Reinitialize the map, call to redraw the map
function initializeMap(markers, redrawMap) {
  // Initialize maps
  if (map === null || (redrawMap != null && redrawMap === true)) {
    if (typeof lat_ !== 'undefined' && typeof long_ !== 'undefined' && lat_ != null && long_ != null) {
      map = newMap(parseFloat(lat_), parseFloat(long_), 7, 'map-canvas', false);
    } else if (typeof state_ !== 'undefined' && typeof city_ !== 'undefined' && state_ != null && city_ != null) {
      geocoder = new google.maps.Geocoder();
      geocoder.geocode( { 'address': city_ + ", " + state_}, function(results, status) {
        if (status === google.maps.GeocoderStatus.OK) {
          map = newMap(results[0].geometry.location.lat(), results[0].geometry.location.lng(), 5, 'map-canvas');
        } else {
          map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
        }
        google.maps.event.addListener(map, 'zoom_changed', handleMapChange);
      });
    } else {
      map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
    }

    google.maps.event.addListener(map, 'zoom_changed', handleMapChange);
  }

  // Initialize markers
  if (map != null && markers != null) {
    drawMarker(map, markers);
  }
  if (map != null) mapBound = map.getBounds();
  return map;
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

function initializePostings(postings) {
  if (postings === null || postings === undefined) {
    return;
  }

  var table = document.getElementById('table_body');
  for(var i = table.rows.length - 1; i >= 0; --i)
  {
      table.deleteRow(i);
  }

  for (var i = 0; i < postings.length; ++i)
  {
    if (stateFilter != null && postings[i]['state'] != stateFilter) {
      continue;
    }

    if (!postings[i]['city']) {
      continue;
    }

    lat = postings[i]['lat'];
    lng = postings[i]['lng'];

    var postingLocation = new google.maps.LatLng(lat, lng);

    if (!mapBound.contains(postingLocation)) {
     continue;
    }

    var row = table.insertRow(table.length);
    
    var index = 0;

    // Date/Location Posted cell
    var dateLocationPosted = row.insertCell(index++);
    var url = '/posting/' + postings[i]['url'];
    dateLocationPosted.innerHTML = postings[i]['datePosted'] + " - " + postings[i]['city'];
    dateLocationPosted.setAttribute('class','col-xs-2 col-sm-2 col-md-2 col-lg-2');

    // Title cell
    var title = row.insertCell(index++);
    title.innerHTML = '<strong>' + postings[i]['title'] + '</strong>';
    title.setAttribute('class','col-xs-8 col-sm-8 col-md-6 col-lg-6 text-center');

    // Code for query with grouping
    // Quantity cell
    var quantity = row.insertCell(index++);
    var quantityString = "";
    if (!postings[i]['quantity']) {
      quantityString = 'Check Quantity!'
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

    quantity.innerHTML = '<a href="' + url + '">' + quantityString + '</a>';
    quantity.setAttribute('class','col-xs-2 col-sm-2 col-md-2 col-lg-2');

    // Price cell
    var price = row.insertCell(index++);

    var priceString = "";
    if (!postings[i]['price']) {
      priceString = 'Check Price!'
    } else {
      for (var j = 0; j < postings[i]['price'].length; ++j) {
        if (j > 6) {
          priceString += "..."
          break;
        }
        priceString += "$" + postings[i]['price'][j] + ", "
      }
    }

    price.innerHTML = '<a href="' + url + '">' + priceString + '</a>';
    price.setAttribute('class','hidden-xs hidden-sm col-md-2 col-lg-2');
  }
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
  // Postings xml request
  var params = location.pathname.split('/');
  var postUrl = "/postings/";
  var priceUrl = "/prices/";
  if (params.length > 2 && params[1] === 'state') {
    postUrl += params[2];
    priceUrl += params[2];
  }

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'postings',
    [false, true, false]);
  xmlhttpPostings.open("POST", postUrl, true);
  xmlhttpPostings.send();

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'prices',
    [false, false, true]);
  xmlhttpPostings.open("POST", priceUrl, true);
  xmlhttpPostings.send();
}

window.google = window.google || {};
google.maps = google.maps || {};

(function() {

  function getScript(src) {
    document.write('<' + 'script src="' + src + '"><' + '/script>');
  }

  var modules = google.maps.modules = {};
  google.maps.__gjsload__ = function(name, text) {
    modules[name] = text;
  };

  google.maps.Load = function(apiLoad) {
    delete google.maps.Load;
    apiLoad([0.009999999776482582,[[["https://mts0.googleapis.com/vt?lyrs=m@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.googleapis.com/vt?lyrs=m@292000000\u0026src=api\u0026hl=en-US\u0026"],null,null,null,null,"m@292000000",["https://mts0.google.com/vt?lyrs=m@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.google.com/vt?lyrs=m@292000000\u0026src=api\u0026hl=en-US\u0026"]],[["https://khms0.googleapis.com/kh?v=165\u0026hl=en-US\u0026","https://khms1.googleapis.com/kh?v=165\u0026hl=en-US\u0026"],null,null,null,1,"165",["https://khms0.google.com/kh?v=165\u0026hl=en-US\u0026","https://khms1.google.com/kh?v=165\u0026hl=en-US\u0026"]],[["https://mts0.googleapis.com/vt?lyrs=h@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.googleapis.com/vt?lyrs=h@292000000\u0026src=api\u0026hl=en-US\u0026"],null,null,null,null,"h@292000000",["https://mts0.google.com/vt?lyrs=h@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.google.com/vt?lyrs=h@292000000\u0026src=api\u0026hl=en-US\u0026"]],[["https://mts0.googleapis.com/vt?lyrs=t@132,r@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.googleapis.com/vt?lyrs=t@132,r@292000000\u0026src=api\u0026hl=en-US\u0026"],null,null,null,null,"t@132,r@292000000",["https://mts0.google.com/vt?lyrs=t@132,r@292000000\u0026src=api\u0026hl=en-US\u0026","https://mts1.google.com/vt?lyrs=t@132,r@292000000\u0026src=api\u0026hl=en-US\u0026"]],null,null,[["https://cbks0.googleapis.com/cbk?","https://cbks1.googleapis.com/cbk?"]],[["https://khms0.googleapis.com/kh?v=84\u0026hl=en-US\u0026","https://khms1.googleapis.com/kh?v=84\u0026hl=en-US\u0026"],null,null,null,null,"84",["https://khms0.google.com/kh?v=84\u0026hl=en-US\u0026","https://khms1.google.com/kh?v=84\u0026hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt?hl=en-US\u0026","https://mts1.googleapis.com/mapslt?hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt/ft?hl=en-US\u0026","https://mts1.googleapis.com/mapslt/ft?hl=en-US\u0026"]],[["https://mts0.googleapis.com/vt?hl=en-US\u0026","https://mts1.googleapis.com/vt?hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt/loom?hl=en-US\u0026","https://mts1.googleapis.com/mapslt/loom?hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt?hl=en-US\u0026","https://mts1.googleapis.com/mapslt?hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt/ft?hl=en-US\u0026","https://mts1.googleapis.com/mapslt/ft?hl=en-US\u0026"]],[["https://mts0.googleapis.com/mapslt/loom?hl=en-US\u0026","https://mts1.googleapis.com/mapslt/loom?hl=en-US\u0026"]]],["en-US","US",null,0,null,null,"https://maps.gstatic.com/mapfiles/","https://csi.gstatic.com","https://maps.googleapis.com","https://maps.googleapis.com",null,"https://maps.google.com","https://csi.gstatic.com","https://maps.gstatic.com/maps-api-v3/api/images/"],["https://maps.gstatic.com/maps-api-v3/api/js/19/9","3.19.9"],[4185773453],1,null,null,null,null,null,"",null,null,1,"https://khms.googleapis.com/mz?v=165\u0026","AIzaSyA59fRrwKrIwATNyiob1CeXnhowhtcVtn8","https://earthbuilder.googleapis.com","https://earthbuilder.googleapis.com",null,"https://mts.googleapis.com/vt/icon",[["https://mts0.googleapis.com/vt","https://mts1.googleapis.com/vt"],["https://mts0.googleapis.com/vt","https://mts1.googleapis.com/vt"],null,null,null,null,null,null,null,null,null,null,["https://mts0.google.com/vt","https://mts1.google.com/vt"],"/maps/vt",292000000,132],2,500,[null,"https://g0.gstatic.com/landmark/tour","https://g0.gstatic.com/landmark/config","","https://www.google.com/maps/preview/log204","","https://static.panoramio.com.storage.googleapis.com/photos/",["https://geo0.ggpht.com/cbk","https://geo1.ggpht.com/cbk","https://geo2.ggpht.com/cbk","https://geo3.ggpht.com/cbk"]],["https://www.google.com/maps/api/js/master?pb=!1m2!1u19!2s9!2sen-US!3sUS!4s19/9","https://www.google.com/maps/api/js/widget?pb=!1m2!1u19!2s9!2sen-US"],null,0,0], loadScriptTime);
  };

  var loadScriptTime = (new Date).getTime();
  getScript("https://maps.gstatic.com/maps-api-v3/api/js/19/9/main.js");
})();

function d(a){return function(b){this[a]=b}}function f(a){return function(){return this[a]}}var k;
function l(a,b,c){this.extend(l,google.maps.OverlayView);this.b=a;this.a=[];this.f=[];this.da=[53,56,66,78,90];this.j=[];this.A=!1;c=c||{};this.g=c.gridSize||60;this.l=c.minimumClusterSize||2;this.K=c.maxZoom||null;this.j=c.styles||[];this.Y=c.imagePath||this.R;this.X=c.imageExtension||this.Q;this.P=!0;void 0!=c.zoomOnClick&&(this.P=c.zoomOnClick);this.r=!1;void 0!=c.averageCenter&&(this.r=c.averageCenter);m(this);this.setMap(a);this.L=this.b.getZoom();var e=this;google.maps.event.addListener(this.b,
"zoom_changed",function(){var a=e.b.getZoom(),b=e.b.minZoom||0,c=Math.min(e.b.maxZoom||100,e.b.mapTypes[e.b.getMapTypeId()].maxZoom),a=Math.min(Math.max(a,b),c);e.L!=a&&(e.L=a,e.m())});google.maps.event.addListener(this.b,"idle",function(){e.i()});b&&(b.length||Object.keys(b).length)&&this.C(b,!1)}k=l.prototype;k.R="http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/images/m";k.Q="png";
k.extend=function(a,b){return function(a){for(var b in a.prototype)this.prototype[b]=a.prototype[b];return this}.apply(a,[b])};k.onAdd=function(){this.A||(this.A=!0,p(this))};k.draw=function(){};function m(a){if(!a.j.length)for(var b=0,c;c=a.da[b];b++)a.j.push({url:a.Y+(b+1)+"."+a.X,height:c,width:c})}k.T=function(){for(var a=this.o(),b=new google.maps.LatLngBounds,c=0,e;e=a[c];c++)b.extend(e.getPosition());this.b.fitBounds(b)};k.w=f("j");k.o=f("a");k.W=function(){return this.a.length};k.ca=d("K");
k.J=f("K");k.G=function(a,b){for(var c=0,e=a.length,g=e;0!==g;)g=parseInt(g/10,10),c++;c=Math.min(c,b);return{text:e,index:c}};k.aa=d("G");k.H=f("G");k.C=function(a,b){if(a.length)for(var c=0,e;e=a[c];c++)s(this,e);else if(Object.keys(a).length)for(e in a)s(this,a[e]);b||this.i()};function s(a,b){b.s=!1;b.draggable&&google.maps.event.addListener(b,"dragend",function(){b.s=!1;a.M()});a.a.push(b)}k.q=function(a,b){s(this,a);b||this.i()};
function t(a,b){var c=-1;if(a.a.indexOf)c=a.a.indexOf(b);else for(var e=0,g;g=a.a[e];e++)if(g==b){c=e;break}if(-1==c)return!1;b.setMap(null);a.a.splice(c,1);return!0}k.Z=function(a,b){var c=t(this,a);return!b&&c?(this.m(),this.i(),!0):!1};k.$=function(a,b){for(var c=!1,e=0,g;g=a[e];e++)g=t(this,g),c=c||g;if(!b&&c)return this.m(),this.i(),!0};k.V=function(){return this.f.length};k.getMap=f("b");k.setMap=d("b");k.I=f("g");k.ba=d("g");
k.v=function(a){var b=this.getProjection(),c=new google.maps.LatLng(a.getNorthEast().lat(),a.getNorthEast().lng()),e=new google.maps.LatLng(a.getSouthWest().lat(),a.getSouthWest().lng()),c=b.fromLatLngToDivPixel(c);c.x+=this.g;c.y-=this.g;e=b.fromLatLngToDivPixel(e);e.x-=this.g;e.y+=this.g;c=b.fromDivPixelToLatLng(c);b=b.fromDivPixelToLatLng(e);a.extend(c);a.extend(b);return a};k.S=function(){this.m(!0);this.a=[]};
k.m=function(a){for(var b=0,c;c=this.f[b];b++)c.remove();for(b=0;c=this.a[b];b++)c.s=!1,a&&c.setMap(null);this.f=[]};k.M=function(){var a=this.f.slice();this.f.length=0;this.m();this.i();window.setTimeout(function(){for(var b=0,c;c=a[b];b++)c.remove()},0)};k.i=function(){p(this)};
function p(a){if(a.A)for(var b=new google.maps.LatLngBounds(a.b.getBounds().getSouthWest(),a.b.getBounds().getNorthEast()),b=a.v(b),c=0,e;e=a.a[c];c++)if(!e.s&&b.contains(e.getPosition())){for(var g=a,u=4E4,q=null,x=0,n=void 0;n=g.f[x];x++){var h=n.getCenter();if(h){var r=e.getPosition();if(h&&r)var y=(r.lat()-h.lat())*Math.PI/180,z=(r.lng()-h.lng())*Math.PI/180,h=Math.sin(y/2)*Math.sin(y/2)+Math.cos(h.lat()*Math.PI/180)*Math.cos(r.lat()*Math.PI/180)*Math.sin(z/2)*Math.sin(z/2),h=12742*Math.atan2(Math.sqrt(h),
Math.sqrt(1-h));else h=0;h<u&&(u=h,q=n)}}q&&q.F.contains(e.getPosition())?q.q(e):(n=new v(g),n.q(e),g.f.push(n))}}function v(a){this.k=a;this.b=a.getMap();this.g=a.I();this.l=a.l;this.r=a.r;this.d=null;this.a=[];this.F=null;this.n=new w(this,a.w())}k=v.prototype;
k.q=function(a){var b;a:if(this.a.indexOf)b=-1!=this.a.indexOf(a);else{b=0;for(var c;c=this.a[b];b++)if(c==a){b=!0;break a}b=!1}if(b)return!1;this.d?this.r&&(c=this.a.length+1,b=(this.d.lat()*(c-1)+a.getPosition().lat())/c,c=(this.d.lng()*(c-1)+a.getPosition().lng())/c,this.d=new google.maps.LatLng(b,c),A(this)):(this.d=a.getPosition(),A(this));a.s=!0;this.a.push(a);b=this.a.length;b<this.l&&a.getMap()!=this.b&&a.setMap(this.b);if(b==this.l)for(c=0;c<b;c++)this.a[c].setMap(null);b>=this.l&&a.setMap(null);
a=this.b.getZoom();if((b=this.k.J())&&a>b)for(a=0;b=this.a[a];a++)b.setMap(this.b);else this.a.length<this.l?B(this.n):(b=this.k.H()(this.a,this.k.w().length),this.n.setCenter(this.d),a=this.n,a.B=b,a.c&&(a.c.innerHTML=b.text),b=Math.max(0,a.B.index-1),b=Math.min(a.j.length-1,b),b=a.j[b],a.ea=b.url,a.h=b.height,a.p=b.width,a.N=b.textColor,a.e=b.anchor,a.O=b.textSize,a.D=b.backgroundPosition,this.n.show());return!0};
k.getBounds=function(){for(var a=new google.maps.LatLngBounds(this.d,this.d),b=this.o(),c=0,e;e=b[c];c++)a.extend(e.getPosition());return a};k.remove=function(){this.n.remove();this.a.length=0;delete this.a};k.U=function(){return this.a.length};k.o=f("a");k.getCenter=f("d");function A(a){var b=new google.maps.LatLngBounds(a.d,a.d);a.F=a.k.v(b)}k.getMap=f("b");
function w(a,b){a.k.extend(w,google.maps.OverlayView);this.j=b;this.u=a;this.d=null;this.b=a.getMap();this.B=this.c=null;this.t=!1;this.setMap(this.b)}k=w.prototype;
k.onAdd=function(){this.c=document.createElement("DIV");if(this.t){var a=C(this,this.d);this.c.style.cssText=D(this,a);this.c.innerHTML=this.B.text}this.getPanes().overlayMouseTarget.appendChild(this.c);var b=this;google.maps.event.addDomListener(this.c,"click",function(){var a=b.u.k;google.maps.event.trigger(a,"clusterclick",b.u);a.P&&b.b.fitBounds(b.u.getBounds())})};function C(a,b){var c=a.getProjection().fromLatLngToDivPixel(b);c.x-=parseInt(a.p/2,10);c.y-=parseInt(a.h/2,10);return c}
k.draw=function(){if(this.t){var a=C(this,this.d);this.c.style.top=a.y+"px";this.c.style.left=a.x+"px"}};function B(a){a.c&&(a.c.style.display="none");a.t=!1}k.show=function(){if(this.c){var a=C(this,this.d);this.c.style.cssText=D(this,a);this.c.style.display=""}this.t=!0};k.remove=function(){this.setMap(null)};k.onRemove=function(){this.c&&this.c.parentNode&&(B(this),this.c.parentNode.removeChild(this.c),this.c=null)};k.setCenter=d("d");
function D(a,b){var c=[];c.push("background-image:url("+a.ea+");");c.push("background-position:"+(a.D?a.D:"0 0")+";");"object"===typeof a.e?("number"===typeof a.e[0]&&0<a.e[0]&&a.e[0]<a.h?c.push("height:"+(a.h-a.e[0])+"px; padding-top:"+a.e[0]+"px;"):c.push("height:"+a.h+"px; line-height:"+a.h+"px;"),"number"===typeof a.e[1]&&0<a.e[1]&&a.e[1]<a.p?c.push("width:"+(a.p-a.e[1])+"px; padding-left:"+a.e[1]+"px;"):c.push("width:"+a.p+"px; text-align:center;")):c.push("height:"+a.h+"px; line-height:"+a.h+
"px; width:"+a.p+"px; text-align:center;");c.push("cursor:pointer; top:"+b.y+"px; left:"+b.x+"px; color:"+(a.N?a.N:"black")+"; position:absolute; font-size:"+(a.O?a.O:11)+"px; font-family:Arial,sans-serif; font-weight:bold");return c.join("")}window.MarkerClusterer=l;l.prototype.addMarker=l.prototype.q;l.prototype.addMarkers=l.prototype.C;l.prototype.clearMarkers=l.prototype.S;l.prototype.fitMapToMarkers=l.prototype.T;l.prototype.getCalculator=l.prototype.H;l.prototype.getGridSize=l.prototype.I;
l.prototype.getExtendedBounds=l.prototype.v;l.prototype.getMap=l.prototype.getMap;l.prototype.getMarkers=l.prototype.o;l.prototype.getMaxZoom=l.prototype.J;l.prototype.getStyles=l.prototype.w;l.prototype.getTotalClusters=l.prototype.V;l.prototype.getTotalMarkers=l.prototype.W;l.prototype.redraw=l.prototype.i;l.prototype.removeMarker=l.prototype.Z;l.prototype.removeMarkers=l.prototype.$;l.prototype.resetViewport=l.prototype.m;l.prototype.repaint=l.prototype.M;l.prototype.setCalculator=l.prototype.aa;
l.prototype.setGridSize=l.prototype.ba;l.prototype.setMaxZoom=l.prototype.ca;l.prototype.onAdd=l.prototype.onAdd;l.prototype.draw=l.prototype.draw;v.prototype.getCenter=v.prototype.getCenter;v.prototype.getSize=v.prototype.U;v.prototype.getMarkers=v.prototype.o;w.prototype.onAdd=w.prototype.onAdd;w.prototype.draw=w.prototype.draw;w.prototype.onRemove=w.prototype.onRemove;Object.keys=Object.keys||function(a){var b=[],c;for(c in a)a.hasOwnProperty(c)&&b.push(c);return b};
