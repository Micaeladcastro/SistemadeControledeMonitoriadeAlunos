package br.ucsal.monitoria.report;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Pequeno ajudante para montar PDFs com titulo, identificacao do emissor, secoes e tabelas. */
public class PdfBuilder {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font PEQUENA = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY);
    private static final Font CABECALHO_TABELA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELULA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color COR_CABECALHO = new Color(31, 78, 121);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private final Document documento;

    public PdfBuilder(String titulo, String emitidoPor, boolean paisagem) {
        documento = new Document(paisagem ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(documento, saida);
        documento.open();
        documento.add(new Paragraph("Sistema de Controle de Monitoria de Alunos", PEQUENA));
        Paragraph p = new Paragraph(titulo, TITULO);
        p.setSpacingAfter(4);
        documento.add(p);
        documento.add(new Paragraph("Emitido por: " + emitidoPor + "  |  Em: " + LocalDateTime.now().format(DATA_HORA), PEQUENA));
        documento.add(Chunk.NEWLINE);
    }

    public PdfBuilder secao(String texto) {
        Paragraph p = new Paragraph(texto, SUBTITULO);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        documento.add(p);
        return this;
    }

    public PdfBuilder campo(String rotulo, Object valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(rotulo + ": ", NEGRITO));
        p.add(new Phrase(valor == null || valor.toString().isBlank() ? "-" : valor.toString(), NORMAL));
        documento.add(p);
        return this;
    }

    public PdfBuilder texto(String texto) {
        documento.add(new Paragraph(texto == null || texto.isBlank() ? "-" : texto, NORMAL));
        return this;
    }

    public PdfBuilder tabela(String[] cabecalhos, Iterable<Object[]> linhas) {
        PdfPTable tabela = new PdfPTable(cabecalhos.length);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(4);
        tabela.setSpacingAfter(6);
        tabela.setHeaderRows(1);
        for (String c : cabecalhos) {
            PdfPCell cell = new PdfPCell(new Phrase(c, CABECALHO_TABELA));
            cell.setBackgroundColor(COR_CABECALHO);
            cell.setPadding(4);
            tabela.addCell(cell);
        }
        boolean vazia = true;
        for (Object[] linha : linhas) {
            vazia = false;
            for (Object valor : linha) {
                PdfPCell cell = new PdfPCell(new Phrase(valor == null ? "-" : valor.toString(), CELULA));
                cell.setPadding(3);
                tabela.addCell(cell);
            }
        }
        if (vazia) {
            PdfPCell cell = new PdfPCell(new Phrase("Nenhum registro", CELULA));
            cell.setColspan(cabecalhos.length);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(cell);
        }
        documento.add(tabela);
        return this;
    }

    public byte[] gerar() {
        documento.close();
        return saida.toByteArray();
    }
}
