window.onload = function() {
    addSumByDateHandler();
    addFavouriteProducts();
}

function addSumByDateHandler(){
    var statusLabel = document.getElementById('sumByDateStatus');
    var sumByDateSubmit = document.getElementById('sumByDateSubmit');
    sumByDateSubmit.onclick = function() {
        console.log("sending SumByDate request");
        var date = document.getElementById('sumByDateInput').value;
        var xhr = new XMLHttpRequest();
        xhr.timeout = 5000;
        xhr.ontimeout = function(){
            handleTimeoutStatusResult(statusLabel)
        }
        xhr.open('GET', '/api/v1/sumByDate?date='+ date, true);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
        xhr.onreadystatechange = function() {
          if (xhr.readyState != 4) return;
          if (xhr.status != 200) {
            handleErrorStatusResult(statusLabel, xhr);
          } else {
            handleSuccessStatusResult(statusLabel)
            fillSumByDateTable(JSON.parse(xhr.responseText));
          }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function addFavouriteProducts(){
    var statusLabel = document.getElementById('favouriteProductsStatus');
    var favouriteProductsSubmit = document.getElementById('favouriteProductsSubmit');

    favouriteProductsSubmit.onclick = function() {
        console.log("sending favouriteProductsSubmit request");
        var date = document.getElementById('card').value;
        var requestLimit = 3;
        var xhr = new XMLHttpRequest();
        xhr.responseType = "json"
        xhr.timeout = 5000;
        xhr.open('GET', '/api/v1/favouriteProducts?limit=' + requestLimit, true);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.ontimeout = function(){
            handleTimeoutStatusResult(statusLabel)
        }
        xhr.send();
        xhr.onreadystatechange = function() {
          if (xhr.readyState != 4) return;
          if (xhr.status != 200) {
            handleErrorStatusResult(statusLabel, xhr);
          } else {
            handleSuccessStatusResult(statusLabel)
            fillfavouriteProductsTable(JSON.parse(xhr.responseText));
          }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function clearTableDataExceptHeader(table){
    var size = table.rows.length;
    for (var i = 1; i< size; i++){
        table.deleteRow(1)
    }
}

// Response format {"date":"2010-12-01","sum":1241312313.53}
function fillSumByDateTable(response){
    var table = document.getElementById("sumByDateTable");
    clearTableDataExceptHeader(table);
    resultRow = table.insertRow(1);
    resultRow.insertCell().innerHTML = response.date;
    resultRow.insertCell().innerHTML = response.sum;
}
// Response format {favourites:{"name":"Cookies","count":100,"code":123}]}
function fillFavouriteProductsTable(response){
    var table = document.getElementById("favouriteProductsTable");
    clearTableDataExceptHeader(table);
    for (var i = 0; i < response.favourites.length; i++){
        resultRow = table.insertRow(i+1);
        var nameCell = resultRow.insertCell();
        nameCell.innerHTML = response.favourites[i].name;
        var countCell = resultRow.insertCell();
        countCell.innerHTML = response.favourites[i].count;
        countCell.className="number"
        var codeCell = resultRow.insertCell();
        codeCell.innerHTML = response.favourites[i].code;
        codeCell.className="number"
    }
}

function handleSuccessStatusResult(elem){
    elem.innerHTML = 'Выполнен';
    elem.className="success";
}

function handleProcessingStatusResult(elem){
    elem.innerHTML = 'Обработка запроса...';
    elem.className="processing";
}

function handleErrorStatusResult(elem, xhr){
    elem.innerHTML="Ошибка."+xhr.status + ': ' + xhr.statusText;
    elem.className="error";
}

function handleTimeoutStatusResult(elem){
    elem.innerHTML="Ошибка. Операция превысила максимальное время ожидания результата."
    elem.className="error";
}