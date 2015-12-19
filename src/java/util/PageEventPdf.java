/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author santos
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Phrase;

/*
 * Clase que maneja los eventos de pagina necesarios para agregar un encabezado
 * y conteo de paginas a un documento. El encabezado, definido en onEndPage,
 * consiste en una tabla con 3 celdas que contienen: Frase del encabezado |
 * pagina <numero de pagina> de | total de paginas, con una linea horizontal
 * separando el encabezado del texto
 *
 * Referencia: http://itextpdf.com/examples/iia.php?id=104
 *
 * @author David
 */
public class PageEventPdf extends PdfPageEventHelper {

    private String encabezado;
    PdfTemplate total;
    private Font fontCursiva12 = null;
    private int pageNumber = 1;

    public PageEventPdf(Font fontCursiva12) {
        this.fontCursiva12 = fontCursiva12;
    }

    /*
     * Crea el objecto PdfTemplate el cual contiene el numero total de paginas
     * en el documento
     */
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        total = writer.getDirectContent().createTemplate(30, 16);
    }

    /*
     * Esta es el metodo a llamar cuando ocurra el evento <b>onEndPage</b>, es
     * en este evento donde crearemos el encabeazado de la pagina con los
     * elementos indicados.
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                new Phrase(writer.getPageNumber() + "", fontCursiva12), (document.right() + document.left()) / 2, document.bottom() - 18, 0);
        pageNumber++;
//        PdfPTable table = new PdfPTable(3);
//        try {
//            // Se determina el ancho y altura de la tabla 
//            table.setWidths(new int[]{24, 24, 2});
//            table.setTotalWidth(527);
//            table.setLockedWidth(true);
//            table.getDefaultCell().setFixedHeight(20);
//
//            // Borde de la celda
//            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
//
//            table.addCell(encabezado);
//            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
//
//            table.addCell(String.format("Pagina % 010d de", writer.getPageNumber()));
//
//            PdfPCell cell = new PdfPCell(Image.getInstance(total));
//
//            cell.setBorder(Rectangle.BOTTOM);
//
//            table.addCell(cell);
//            // Esta linea escribe la tabla como encabezado
//            table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
//        } catch (DocumentException de) {
//            throw new ExceptionConverter(de);
//        }
    }

    /*
     * Realiza el conteo de paginas al momento de cerrar el documento
     */
    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
    }

    // Getter and Setters
    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public Font getFontCursiva12() {
        return fontCursiva12;
    }

    public void setFontCursiva12(Font fontCursiva12) {
        this.fontCursiva12 = fontCursiva12;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

}
