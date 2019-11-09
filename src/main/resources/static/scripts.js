window.onload = function() {
    addSumByDateHandler();
    addFavoriteProducts();
}

function addSumByDateHandler(){
    var statusLabel = document.getElementById('sumByDateStatus');
    var sumByDateSubmit = document.getElementById('sumByDateSubmit');
    sumByDateSubmit.onclick = function() {
        console.log("sending SumByDate request");
        var date = document.getElementById('sumByDateInput').value;
        var xhr = new XMLHttpRequest();
        xhr.open('GET', '/api/sumByDate?='+date, true);
        xhr.send();
        xhr.onreadystatechange = function() {
          if (xhr.readyState != 4) return;
          if (xhr.status != 200) {
            handleErrorStatusResult(statusLabel, xhr);
          } else {
            handleProcessingStatusResult(statusLabel)
          }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function addFavoriteProducts(){
    var statusLabel = document.getElementById('favoriteProductsStatus');
    var favoriteProductsSubmit = document.getElementById('favoriteProductsSubmit');
    favoriteProductsSubmit.onclick = function() {
        console.log("sending favoriteProductsSubmit request");
        var date = document.getElementById('card').value;
        var xhr = new XMLHttpRequest();
        xhr.open('GET', '/api/favoriteProducts?='+date, true);
        xhr.send();
        xhr.onreadystatechange = function() {
          if (xhr.readyState != 4) return;
          if (xhr.status != 200) {
            handleErrorStatusResult(statusLabel, xhr);
          } else {
            handleProcessingStatusResult(statusLabel)
          }
        }
        handleProcessingStatusResult(statusLabel)
    }
}

function handleSuccessStatusResult(elem){
    statusLabel.innerHTML = 'Выполнен';
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