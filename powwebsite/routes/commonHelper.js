CommonHelper = function() {
};

CommonHelper.prototype.ReplaceAll = function(find, replace, str) {
    return str.replace(new RegExp(find, 'g'), replace);
}

CommonHelper.prototype.SortNumber = function(a,b) {
    return a - b;
}

CommonHelper.prototype.IsIntValue = function(value) {
    if ((parseFloat(value) == parseInt(value)) && !isNaN(value)){
      return true;
    }

    return false;
}

CommonHelper.prototype.ParseArrayString = function(str, split, lowerBound, upperBound, isInt) {
    if (!str) {
        return str;
    }

    var array = [];
    if (str[0] == '[') {
        str = str.substring(1);
    }
    if (str[str.length - 1] == ']') {
        str = str.substring(0, str.length - 1);
    }
    array = str.split(split);

    var sortedArray = [];
    for (var i = 0; i < array.length; ++i) {
        var parsedValue = parseFloat(array[i]);
        if (!isNaN(parsedValue)) {
            if (!isInt || this.IsIntValue(parsedValue)) {
                sortedArray.push(parsedValue);
            }
        }
    }
    sortedArray.sort(this.SortNumber);

    var uniqueSortedArray = [];
    for (var i = 0; i < sortedArray.length; ++i) {
        if (!isNaN(sortedArray[i]) && sortedArray[i] >= lowerBound && sortedArray[i] <= upperBound && (i == 0 || sortedArray[i] != sortedArray[i-1])) {
            var divisibleBy125 = Math.round(sortedArray[i] * 1000) % 25;
            if (divisibleBy125 == 0) {
                uniqueSortedArray.push(sortedArray[i]);
            }
        }
    }

    return uniqueSortedArray;
}

exports.CommonHelper = CommonHelper;