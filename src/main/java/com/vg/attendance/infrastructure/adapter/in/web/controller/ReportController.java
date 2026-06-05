package com.vg.attendance.infrastructure.adapter.in.web.controller;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vg.attendance.application.port.out.AttendanceRepositoryPort;
import com.vg.attendance.domain.model.Attendance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/attendance/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AttendanceRepositoryPort attendanceRepository;

    @Value("${school.name:Colegio Secundaria Peru}")
    private String schoolName;

    @GetMapping("/daily")
    public Mono<ResponseEntity<byte[]>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Generando reporte de asistencias para fecha: {}", date);

        return attendanceRepository.findByFecha(date)
            .collectList()
            .flatMap(attendances -> {
                if (attendances.isEmpty()) {
                    return generarPdfVacio(date);
                }
                return generarPdfConDatos(attendances, date);
            })
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return generarPdfError(date, e.getMessage());
            });
    }

    private Mono<ResponseEntity<byte[]>> generarPdfVacio(LocalDate date) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("No hay asistencias registradas para esta fecha"));
            doc.add(new Paragraph("Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            doc.close();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_asistencia_" + date + ".pdf")
                    .body(out.toByteArray());
        });
    }

    private Mono<ResponseEntity<byte[]>> generarPdfError(LocalDate date, String error) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Error generando reporte: " + error));
            doc.close();
            
            return ResponseEntity.status(500)
                    .body(out.toByteArray());
        });
    }

    private Mono<ResponseEntity<byte[]>> generarPdfConDatos(List<Attendance> attendances, LocalDate date) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, out);
        doc.open();

        Paragraph titulo = new Paragraph(schoolName, new Font(Font.HELVETICA, 14, Font.BOLD));
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);
        
        Paragraph subtitulo = new Paragraph("REPORTE DE ASISTENCIA DIARIO", new Font(Font.HELVETICA, 16, Font.BOLD));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitulo);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        doc.add(new Paragraph("Total de asistencias registradas: " + attendances.size()));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{10, 20, 12, 12, 30, 16});

        String[] encabezados = {"ID", "Estudiante ID", "Clase ID", "Estado", "Hora", "Justificación"};
        for (String enc : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(enc, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            celda.setBackgroundColor(new Color(0, 51, 102));
            celda.setPadding(8);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celda);
        }

        for (Attendance a : attendances) {
            String estadoTexto;
            Color estadoColor;
            switch (a.getEstado()) {
                case "A": estadoTexto = "ASISTIÓ"; estadoColor = new Color(40, 167, 69); break;
                case "T": estadoTexto = "TARDANZA"; estadoColor = new Color(255, 193, 7); break;
                case "J": estadoTexto = "JUSTIFICADO"; estadoColor = new Color(0, 123, 255); break;
                case "F": estadoTexto = "FALTÓ"; estadoColor = new Color(220, 53, 69); break;
                default: estadoTexto = a.getEstado(); estadoColor = Color.BLACK;
            }

            tabla.addCell(new PdfPCell(new Phrase(a.getId().toString(), new Font(Font.HELVETICA, 9))));
            tabla.addCell(new PdfPCell(new Phrase(a.getEstudianteId(), new Font(Font.HELVETICA, 9))));
            tabla.addCell(new PdfPCell(new Phrase(a.getClaseId(), new Font(Font.HELVETICA, 9))));
            
            PdfPCell celdaEstado = new PdfPCell(new Phrase(estadoTexto, new Font(Font.HELVETICA, 9, Font.BOLD, estadoColor)));
            celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaEstado);
            
            String hora = a.getHoraLlegada() != null ? a.getHoraLlegada().toString() : "-";
            PdfPCell celdaHora = new PdfPCell(new Phrase(hora, new Font(Font.HELVETICA, 9)));
            celdaHora.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaHora);
            
            String justificacion = a.getJustificacionNota() != null && !a.getJustificacionNota().isEmpty() 
                ? a.getJustificacionNota() : "-";
            tabla.addCell(new PdfPCell(new Phrase(justificacion, new Font(Font.HELVETICA, 8))));
        }
        
        doc.add(tabla);
        doc.close();

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_asistencia_" + date + ".pdf")
                .body(out.toByteArray()));
    }
}
