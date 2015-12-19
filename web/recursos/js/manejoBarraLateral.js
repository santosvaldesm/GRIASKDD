var canvasE;
var mX;
var mY;
function getPosicion(canvas, evt) {
    var rect = canvas.getBoundingClientRect();
    mX = evt.clientX - rect.left;
    mY = evt.clientY - rect.top;
}
var canvasE;
var mX;
var mY;
function getPosicion(canvas, evt) {
    var rect = canvas.getBoundingClientRect();
    mX = evt.clientX - rect.left;
    mY = evt.clientY - rect.top;
}
function handleDrop(event, ui) {
    canvasE = document.getElementById('IdCanvasEventos');
    getPosicion(canvasE, event);
    //console.warn('mx:' + mX + 'my:' + mY);
    try {
        rcCreateNode([{name: 'newNodeData', value: ui.helper.html().replace(" ", "") + ';' + parseInt(mX - 25) + ';' + parseInt(mY - 25) + ";" + "_r"}]);
    }
    catch (err) {
        console.warn(err.message + '---------------------------------');
    }
}
//$(function () {//FUNCION QUE SE EJECUTA AL CARGAR LA PAGINA
//    canvasE = document.getElementById('IdCanvasEventos');
//    $(".subMenu").bind("click", function () {//CLICK EN SUBMENU CIERRA LOS OTROS SUMENUS
//        resetMenu();
//    });
//    $(".draggable").draggable({revert: true});
//    $(".droppable").droppable({
//        drop: function (event, ui) {//SUELTA OBJETO DRAGABLE ENCIMA DE OBJETO DROPABLE
//            getPosicion(canvasE, event);
//            rcCreateNode([{name: 'newNodeData', value: ui.helper.children(0).text().replace(" ", "") + ';' + parseInt(mX - 25) + ';' + parseInt(mY - 25) + ";" + "_r"}]);
//        }
//    });
//    //resetMenu();
//});
//function resetMenu() {//CIERRA TODOS LOS MENUS
//    $('#IdDivMenu').find('li').each(function (indice, elemento) {
//        if ($(this).attr('class').indexOf('ui-menuitem') > -1) {
//            $(this).attr('style', 'display: none;');
//        } else {
//            $(this).children().children().attr('class', 'ui-icon ui-icon-triangle-1-e');
//            $(this).attr("aria-expanded", "true");
//        }
//    });
//}