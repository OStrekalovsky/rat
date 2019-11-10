window.onload = function() {
    addSumByDateHandler();
    addFavouriteProducts();
}

function addSumByDateHandler() {
    var statusLabel = document.getElementById('sumByDateStatus');
    var sumByDateSubmit = document.getElementById('sumByDateSubmit');
    var table = document.getElementById("sumByDateTable");
    sumByDateSubmit.onclick = function() {
        console.log("sending SumByDate request");
        clearTableDataExceptHeader(table);
        var date = document.getElementById('sumByDateInput').value;
        var xhr = new XMLHttpRequest();
        xhr.timeout = 5000;
        xhr.ontimeout = function() {
            handleTimeoutStatusResult(statusLabel)
        }
        xhr.open('GET', '/api/v1/sumByDate?date=' + date, true);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
        xhr.onreadystatechange = function() {
            if (xhr.readyState != 4) return;
            if (xhr.status != 200) {
                handleErrorStatusResult(statusLabel, "Код " + xhr.status);
            } else {
                handleSuccessStatusResult(statusLabel)
                fillSumByDateTable(JSON.parse(xhr.responseText), table);
            }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function addFavouriteProducts() {
    var statusLabel = document.getElementById('favouriteProductsStatus');
    var favouriteProductsSubmit = document.getElementById('favouriteProductsSubmit');
    var table = document.getElementById("favouriteProductsTable");
    favouriteProductsSubmit.onclick = function() {
        console.log("sending favouriteProductsSubmit request");
        clearTableDataExceptHeader(table);
        var card = document.getElementById('card').value;
        var xhr = new XMLHttpRequest();
        xhr.timeout = 5000;
        xhr.open('GET', '/api/v1/favouriteProducts?card=' + card, true);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.ontimeout = function() {
            handleTimeoutStatusResult(statusLabel)
        }
        xhr.send();
        xhr.onreadystatechange = function() {
            if (xhr.readyState != 4) return;
            if (xhr.status != 200 && xhr.status != 404) {
                handleErrorStatusResult(statusLabel, "Код "+ xhr.status);
            } else if (xhr.status == 404) {
                handleErrorStatusResult(statusLabel, "Данных по карте нет");
            }else {
                handleSuccessStatusResult(statusLabel)
                fillFavouriteProductsTable(JSON.parse(xhr.responseText), table);
            }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function clearTableDataExceptHeader(table) {
    var size = table.rows.length;
    for (var i = 1; i < size; i++) {
        table.deleteRow(1)
    }
}

function formatISODate(dateString){
    /*
       I had to force the locale so that all browsers had the same result
       because they read locale settings differently, which leads to inconsistency in UI.
    */
    return new Intl.DateTimeFormat('ru-Ru').format(new Date(dateString));
}

function formatNumber(moneyString){
    /*
       I had to force the locale so that all browsers had the same result
       because they read locale settings differently, which leads to inconsistency in UI.
    */
    return new Intl.NumberFormat('ru-Ru').format(moneyString);
}

// Response format {"date":"2010-12-01","sum":1241312313.53}
function fillSumByDateTable(response, table) {
    resultRow = table.insertRow(1);
    resultRow.insertCell().innerHTML = formatISODate(response.date);
    resultRow.insertCell().innerHTML = response.sum.toLocaleString();
}
// Response format {favourites:{"name":"Cookies","count":100,"code":123}]}
function fillFavouriteProductsTable(response, table) {

    clearTableDataExceptHeader(table);
    for (var i = 0; i < response.favourites.length; i++) {
        resultRow = table.insertRow(i + 1);
        var nameCell = resultRow.insertCell();
        nameCell.innerHTML = response.favourites[i].name;
        var countCell = resultRow.insertCell();
        countCell.innerHTML = formatNumber(response.favourites[i].count)
        countCell.className = "number"
        var codeCell = resultRow.insertCell();
        codeCell.innerHTML = response.favourites[i].code
        codeCell.className = "number"
    }
}

function handleSuccessStatusResult(elem) {
    elem.innerHTML = 'Выполнен';
    elem.className = "success";
}

function handleProcessingStatusResult(elem) {
    elem.innerHTML = 'Обработка запроса...';
    elem.className = "processing";
}

function handleErrorStatusResult(elem, details) {
    elem.innerHTML = "Ошибка. " + details;
    elem.className = "error";
}

function handleTimeoutStatusResult(elem) {
    elem.innerHTML = "Ошибка. Операция превысила максимальное время ожидания результата."
    elem.className = "error";
}