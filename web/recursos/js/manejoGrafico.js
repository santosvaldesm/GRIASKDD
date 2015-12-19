////////////////////////////////////////////////////////////////////////////////
////////////////// DECLARACION DE VARIABLES ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//var canvasBarraInformacion = document.getElementById('IdBarraInformacion');
//var contextBarraInformacion = canvasBarraInformacion.getContext('2d');
var canvasGrafico = document.getElementById('IdCanvasGrafico');
var contextCanvasGrafico = canvasGrafico.getContext('2d');
var canvasEventos = document.getElementById('IdCanvasEventos');
var contextCanvasEventos = canvasEventos.getContext('2d');
var listaImagenes = null;
var listaNodos = new Array();
var mousePosX = 0;
var mousePosY = 0;
var mousePosXClick = 0;//posicion cuando se presiono el raton
var mousePosYClick = 0;//posicion cuando se presiono el raton
var mousePosXPantalla = 0;
var mousePosYPantalla = 0;
var nodoPresionado = null;
var splitTexto = null;
var splitNodo = null;//divide entre datos nodo y conexiones nodo
var ultimoNodoCliqueado = null;
var estadoConectando = false;
var pos = -1;
var id = -1;


////////////////////////////////////////////////////////////////////////////////
////////////////// FUNCIONES GRAFICAS //////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

//function writeMessage(message) {//escribre un mensaje en la barra de informacion
//    contextBarraInformacion.clearRect(0, 0, canvasBarraInformacion.width, canvasBarraInformacion.height);
//    contextBarraInformacion.font = '11pt Calibri';
//    contextBarraInformacion.fillStyle = 'black';
//    contextBarraInformacion.fillText(message, 3, 13);
//}

function drawShape(x1, y1, x2, y2) {//funcion que dibuja una linea dependiendo de las coordenadas de dos puntos
    contextCanvasGrafico.lineWidth = 1;
    contextCanvasGrafico.beginPath();
    contextCanvasGrafico.lineTo(x1, y1);
    contextCanvasGrafico.lineTo(x2, y2);
    contextCanvasGrafico.stroke();
}

function posicionarConstruirMenu() {//ubica el overlay panel donde se dio click y crea el menu ( funcion recargarContextMenu )
    $("[id='IdFormPrincipal:IdOverlayPanel']").css("top", (mousePosYPantalla) + "px");//Se mueve el overlayPanel a la posicion del raton
    $("[id='IdFormPrincipal:IdOverlayPanel']").css("left", (mousePosXPantalla) + "px");//Se mueve el overlayPanel a la posicion del raton
    id = determinarIdNodoCliqueado(mousePosX, mousePosY);
    rcReloadContextMenu([{name: 'idNodo', value: id}]);//console.warn("El nodo cliqueado es:" + id);
}

function dibujarNodos() {
    if (listaNodos !== null) {
        contextCanvasGrafico.font = '9pt Calibri';
        contextCanvasGrafico.fillStyle = 'black';
        for (var i = 0; i < listaNodos.length; i = i + 1) {//DIBUJO LINEAS
            for (var j = 0; j < listaNodos[i].conexiones.length; j = j + 1) {
                pos = buscarPosicionNodo(listaNodos[i].conexiones[j]);
                drawShape(parseInt(listaNodos[i].x) + 25, parseInt(listaNodos[i].y) + 25, parseInt(listaNodos[pos].x) + 25, parseInt(listaNodos[pos].y) + 25);
            }
        }
        try {
            for (var i = 0; i < listaNodos.length; i = i + 1) {//DIBUJO NODOS                
                contextCanvasGrafico.drawImage(document.getElementById('Id' + listaNodos[i].tipo + listaNodos[i].estado), 0, 0, 50, 50, listaNodos[i].x, listaNodos[i].y, 50, 50);
                contextCanvasGrafico.fillText(listaNodos[i].tipo, parseInt(listaNodos[i].x), parseInt(listaNodos[i].y) + 60);
            }
        }
        catch (err) {
            console.warn(err.message + '---------------------------------');
            for (var i = 0; i < listaNodos.length; i = i + 1) {//DIBUJO NODOS                
                console.warn('Id' + listaNodos[i].tipo + listaNodos[i].estado);                
            }
            //contextCanvasGrafico = canvasGrafico.getContext('2d');

        }
    }
}

function repintarAreaDeTrabajo() {//limpiar y pintar area de trabajo (lineas y nodos)    
    contextCanvasGrafico.clearRect(0, 0, canvasGrafico.width, canvasGrafico.height);
    dibujarNodos();
}

////////////////////////////////////////////////////////////////////////////////
////////////////// COMUNICACION CON MANAGED BEAN ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////
function convertTxtToNodes(n) {//convierte una cadena de texto en un arreglo de nodos tipo java script    
    listaNodos = new Array();
    if (String(n).length !== 0) {//si la cadena tiene longitud es por que si vienen nodos
        var splitTextoNodos = n.split("}");//contiene todos los nodos        
        for (var i = 0; i < splitTextoNodos.length; i = i + 1) {
            splitNodo = splitTextoNodos[i].split("(");//DIVIDE EL TEXTO EN DATOS NODO(splitNodo[0]) Y CONEXIONES(splitNodo[1])                
            splitTexto = splitNodo[0].split(";");//INGRESO LOS NODOS
            listaNodos[i] = new Nodo(splitTexto[0], splitTexto[1], splitTexto[2], splitTexto[3], splitTexto[4]);
            //console.warn('El tipo es: '+splitTexto[1]);
            if (splitNodo.length > 1) {
                splitTexto = splitNodo[1].split(";");//INGRESO LAS CONEXIONES
                for (var j = 0; j < splitTexto.length; j = j + 1) {
                    var r = new Array(splitTexto[j]);
                    listaNodos[i].conexiones = listaNodos[i].conexiones.concat(r);
                }
            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
////////////////// MANEJO DE NODOS /////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

//////////////////// CREACION DE NODO /////////////////////////
function crearNodo(tipo, estado) {//la posicion del raton se detrmino cuando se dio click derecho para visualozar el menu
    rcCreateNode([{name: 'newNodeData', value: tipo + ';' + parseInt(mousePosX - 25) + ';' + parseInt(mousePosY - 25) + ";" + estado}]);
}
//////////////////// ELIMINACION DE NODO //////////////////////
function eliminarNodo() {
    rcRemoveNode([{name: 'removeData', value: ultimoNodoCliqueado.id}]);
}
// BUSCAR POSICION NODO EN EL ARRAY DE NODOS SEGUN UN IDENTIFICADOR ////////////////////////
function buscarPosicionNodo(idNodo) {
    if (listaNodos !== null) {
        for (var i = 0; i < listaNodos.length; i = i + 1) {
            if (parseInt(listaNodos[i].id) === parseInt(idNodo)) {
                return i;
            }
        }
    }
    return -1;
}
/////////MOSTRAR EN CONSOLA ESTADO ACTUAL DEL VECTOR DE NODOS //////
function imprimirDatos() {
    console.warn("CANTIDAD NODOS " + listaNodos.length);
    for (var i = 0; i < listaNodos.length; i = i + 1) {
        console.warn("NODO_" + i + "  ID_" + listaNodos[i].id + "  TIPO_" + listaNodos[i].tipo + "  X_" + listaNodos[i].x + "  Y_" + listaNodos[i].y + " CONEXIONES_" + listaNodos[i].conexiones.length + " ");
        for (var j = 0; j < listaNodos[i].conexiones.length; j = j + 1) {
            console.warn("CONECTADO A " + listaNodos[i].conexiones[j]);
        }
    }
}
//////////////////// DETERMINAR SI ó NO SE ESTA CONECTANDO /////////////////////
function cambiarConectando(booleanEstado) {
    if (booleanEstado === 'true') {
        estadoConectando = true;
    } else {
        estadoConectando = false;
    }
    repintarAreaDeTrabajo();
}
//////////////////// DETRMINAR SI HAY CLICK SOBRE NODO /////////////////////////
function determinarIdNodoCliqueado(px, py) {
    if (listaNodos !== null) {//VERIRIFICAR SI ESTA ENCIMA DE UN NODO
        for (var i = 0; i < listaNodos.length; i = i + 1) {
            if (parseInt(px) <= parseInt(listaNodos[i].x) + 50 && parseInt(px) >= parseInt(listaNodos[i].x) && parseInt(py) <= parseInt(listaNodos[i].y) + 50 && parseInt(py) >= parseInt(listaNodos[i].y)) {
                return listaNodos[i].id;//RETORNA EL IDENTIFICADOR DEL NODO
            }
        }
    }
    return -1;//-1 ES QUE NO ESTA SOBRE NNINGUN NODO
}
////////////////////////////////////////////////////////////////////////////////
////////////////// FUNCIONES RATON ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

//////////////////// DETERMINAR POSICION DEL RATON /////////////////////////////
function getMousePos(canvas, evt) {
    var rect = canvas.getBoundingClientRect();
    mousePosX = evt.clientX - rect.left;//posicion con respecto al area grafica
    mousePosY = evt.clientY - rect.top;
    mousePosXPantalla = evt.clientX;//posicion con respecto a la pagina completa
    mousePosYPantalla = evt.clientY;
}
///////////////////////////// MOVIENDO RATON /////////////////////////////////
canvasEventos.addEventListener('mousemove', function (evt) {
    if (nodoPresionado !== null) {
        document.body.style.cursor = 'move';
    }
    getMousePos(canvasEventos, evt);
    if (estadoConectando === true && ultimoNodoCliqueado !== null) {//DIGUJAR LINEA ENTRE RATON Y ULTIMO NODO CLICLEADO
        repintarAreaDeTrabajo();
        drawShape(mousePosX, mousePosY, parseInt(listaNodos[ultimoNodoCliqueado.pos].x) + 25, parseInt(listaNodos[ultimoNodoCliqueado.pos].y) + 25);
    }
    getMousePos(canvasEventos, evt);
    //writeMessage('Moviendo: ' + mousePosX + ',' + mousePosY);
}, false);
//////////////////// RATON SE PRESIONA /////////////////////////////////////////
canvasEventos.addEventListener("mousedown", function (evt) {
    getMousePos(canvasEventos, evt);
    //writeMessage('Mouse DOWN ' + mousePosX + ',' + mousePosY);
    mousePosXClick = mousePosX;
    mousePosYClick = mousePosY;
    id = determinarIdNodoCliqueado(mousePosX, mousePosY);
    if (parseInt(id) !== -1) {//CLICK ENCIMA DE NODO//console.warn('NODO PRESIONADO iDENTIFICADOR ES: '+id);
        pos = buscarPosicionNodo(id);//BUSCAR POSICION EN ARRAY//console.warn('NODO PRESIONADO POSICION EN ARRAY ES: '+pos);
        nodoPresionado = new Nodo(listaNodos[pos].id, listaNodos[pos].tipo, listaNodos[pos].x, listaNodos[pos].y, listaNodos[pos].estado);
    } else {//console.warn('NO SE PUDO DETERMINAR IDENTIFICADOR DE NODO '+id);
        nodoPresionado = null;
    }
});
/////////////////////// RATON SE SUELTA ////////////////////////////////////////
canvasEventos.addEventListener("mouseup", function (evt) {
    getMousePos(canvasEventos, evt);//writeMessage('Mouse UP ' + mousePosX + ',' + mousePosY);
    if (estadoConectando === true) {
        estadoConectando = false;
        id = determinarIdNodoCliqueado(mousePosX, mousePosY);
        if (parseInt(id) !== -1) {//CREAR CONEXION(LINEA) SI CLICK EN UN NODO
            if (parseInt(id) !== parseInt(ultimoNodoCliqueado.id)) {//NO SE TRATA DEL MISMO NODO
                rcCreateConnection([{name: 'newConnectionData', value: ultimoNodoCliqueado.id + ';' + id}]);
            }
        } else {//SE LIMPIA LA LINEA YA QUE NO SE PUDO CREAR LA CONEXION
            repintarAreaDeTrabajo();
        }
        ultimoNodoCliqueado = null;
    } else if (nodoPresionado !== null) {//HABIA PRESIONADO UN NODO(SE ESTABA ARRASTRANDO)                
        ultimoNodoCliqueado = new NodoRevertir(nodoPresionado.id, buscarPosicionNodo(nodoPresionado.id), nodoPresionado.x, nodoPresionado.y);//EN CASO DE SER CLICK DERECHO PODER REVERTIR MOVIMIENTO        
        if (mousePosXClick !== mousePosX || mousePosYClick !== mousePosY) {//si el raton se solto  en una posicion diferente a la que se presiono se mueve
            rcMoveNode([{name: 'moveData', value: id + ';' + parseInt(mousePosX - 25) + ';' + parseInt(mousePosY - 25)}]);
        }
    }
    document.body.style.cursor = 'auto';
    nodoPresionado = null;
});
/////////////////////// PRESIONA CLICK DERECHO /////////////////////////////////
canvasEventos.addEventListener('contextmenu', function (ev) {//DETERMINAR SI SE PRESION CLICK DERECHO(ABRIR MENU)
    PF('wvContextMenu').hide();
    ev.preventDefault();
    return false;
}, false);
///////////////////////// RATON ENTRA AL AREA DE TRABAJO /////////////////////////
//canvasEventos.addEventListener('mouseenter', function (evt) {
//    writeMessage('ENTRO ' + mousePosX + ',' + mousePosY);
//}, false);
//
///////////////////////// RATON SALE DEL AREA DE TRABAJO /////////////////////////
//canvasEventos.addEventListener('mouseleave', function (evt) {
//    writeMessage('SALIO ' + mousePosX + ',' + mousePosY);
//    nodoPresionado = null;
//    document.body.style.cursor = 'auto';
//}, false);
/////////////////////// DOBLE CLICK //////////////////////////////////////////
//canvasEventos.addEventListener("dblclick", function (evt) {//CON DOBLE CLICK REALIZAR CONEXIONES    
//    getMousePos(canvasEventos, evt);
//    writeMessage('Doble click ' + mousePosX + ',' + mousePosY);
//});

////////////////////////////////////////////////////////////////////////////////
////////////////// CLASES NODOS ////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
function Nodo(id, tipo, x, y, estado) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.tipo = tipo;
    this.estado = estado;
    this.conexiones = new Array();//lista de identificadores a los que se conecta
}

function NodoRevertir(id, pos, x, y) {//se usa para revertir un movimiento(cuando se presiona click derecho no revertie ese movimiento)
    this.pos = pos;//posicion en el vector de nodos
    this.id = id;//posicion en el vector de nodos
    this.x = x;//posiicion que estaba en x(inicialmente por que cambio)
    this.y = y;//posiicion que estaba en y(inicialmente por que cambio)
}

Nodo.prototype.funcionEjemplo = function () {
    //alert ('Hola, Soy ' + this.primerNombre);
    //se llama instanciaNodo.funcionEjemplo();     
};