package com.vg.attendance.infrastructure.adapter.in.web.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vg.attendance.application.port.out.AttendanceRepositoryPort;
import com.vg.attendance.domain.model.Attendance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/attendance/reports")
public class ReportController {

    private final AttendanceRepositoryPort attendanceRepository;

    @Value("${school.name:INSTITUCIÓN EDUCATIVA PÚBLICA MIXTO SAN LUIS}")
    private String schoolName;

    @Value("${school.modular-code:0286427}")
    private String modularCode;

    @Value("${school.local-code:354416}")
    private String localCode;

    public ReportController(AttendanceRepositoryPort attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/class/{classId}")
    public Mono<ResponseEntity<byte[]>> getClassReport(
            @PathVariable String classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Generando reporte de asistencia para clase: {}, fecha: {}", classId, date);

        return attendanceRepository.findByClaseIdAndFecha(classId, date)
            .collectList()
            .flatMap(attendances -> {
                try {
                    return Mono.just(generarPdf(attendances, date, classId));
                } catch (Exception e) {
                    log.error("Error generando PDF: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).build());
                }
            });
    }

    private ResponseEntity<byte[]> generarPdf(List<Attendance> attendances, LocalDate date, String classId) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Header con logo
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{20f, 80f});

        // Logo
        try {
            Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
            logo.scaleToFit(60, 60);
            PdfPCell logocell = new PdfPCell(logo);
            logocell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            headerTable.addCell(logocell);
        } catch (Exception e) {
            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            headerTable.addCell(emptyCell);
        }

        // Información del colegio
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        Paragraph schoolInfo = new Paragraph();
        schoolInfo.add(new Phrase(schoolName, new Font(Font.HELVETICA, 12, Font.BOLD)));
        schoolInfo.add(new Phrase("\n"));
        schoolInfo.add(new Phrase("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                        new Font(Font.HELVETICA, 8, Font.NORMAL)));
        schoolInfo.add(new Phrase("\n"));
        schoolInfo.add(new Phrase("Clase: " + classId + " - Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        new Font(Font.HELVETICA, 9, Font.NORMAL)));
        infoCell.addElement(schoolInfo);
        headerTable.addCell(infoCell);
        doc.add(headerTable);
        
        doc.add(new Paragraph(" "));

        // Título
        Paragraph title = new Paragraph("REPORTE DE ASISTENCIA POR CLASE", new Font(Font.HELVETICA, 14, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph(" "));

        // Estadísticas
        long total = attendances.size();
        long asistieron = attendances.stream().filter(a -> "A".equals(a.getEstado())).count();
        long tardanza = attendances.stream().filter(a -> "T".equals(a.getEstado())).count();
        long justificados = attendances.stream().filter(a -> "J".equals(a.getEstado())).count();
        long faltaron = attendances.stream().filter(a -> "F".equals(a.getEstado())).count();

        PdfPTable stats = new PdfPTable(5);
        stats.setWidthPercentage(100);
        stats.setWidths(new float[]{20f, 20f, 20f, 20f, 20f});
        
        String[] labels = {"Total", "Asistieron", "Tardanza", "Justificados", "Faltaron"};
        long[] values = {total, asistieron, tardanza, justificados, faltaron};
        Color[] colors = {new Color(0,51,102), new Color(40,167,69), new Color(255,193,7), new Color(0,123,255), new Color(220,53,69)};
        
        for (String label : labels) {
            PdfPCell labelCell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            labelCell.setBackgroundColor(new Color(0,51,102));
            labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            labelCell.setPadding(6);
            stats.addCell(labelCell);
        }
        
        for (int i = 0; i < values.length; i++) {
            PdfPCell valueCell = new PdfPCell(new Phrase(String.valueOf(values[i]), new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)));
            valueCell.setBackgroundColor(colors[i]);
            valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            valueCell.setPadding(8);
            stats.addCell(valueCell);
        }
        
        doc.add(stats);
        doc.add(new Paragraph(" "));

        // Tabla de asistencias
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10f, 35f, 15f, 12f, 28f});

        String[] headers = {"#", "Estudiante", "Estado", "Hora", "Justificación"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(new Color(0, 51, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        int counter = 1;
        for (Attendance a : attendances) {
            String estado = "";
            Color estadoColor = Color.BLACK;
            switch (a.getEstado()) {
                case "A": estado = "ASISTIÓ"; estadoColor = new Color(40, 167, 69); break;
                case "T": estado = "TARDANZA"; estadoColor = new Color(255, 193, 7); break;
                case "J": estado = "JUSTIFICADO"; estadoColor = new Color(0, 123, 255); break;
                case "F": estado = "FALTÓ"; estadoColor = new Color(220, 53, 69); break;
                default: estado = a.getEstado();
            }
            
            String estudianteId = a.getEstudianteId();
            if (estudianteId != null && estudianteId.length() > 8) {
                estudianteId = estudianteId.substring(estudianteId.length() - 8);
            }
            
            table.addCell(new PdfPCell(new Phrase(String.valueOf(counter++), new Font(Font.HELVETICA, 9))));
            table.addCell(new PdfPCell(new Phrase(estudianteId, new Font(Font.HELVETICA, 9))));
            
            PdfPCell estadoCell = new PdfPCell(new Phrase(estado, new Font(Font.HELVETICA, 9, Font.BOLD, estadoColor)));
            estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(estadoCell);
            
            String hora = a.getHoraLlegada() != null ? a.getHoraLlegada().toString() : "-";
            PdfPCell horaCell = new PdfPCell(new Phrase(hora, new Font(Font.HELVETICA, 9)));
            horaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(horaCell);
            
            String justificacion = a.getJustificacionNota() != null ? a.getJustificacionNota() : "-";
            if (justificacion.length() > 35) justificacion = justificacion.substring(0, 32) + "...";
            table.addCell(new PdfPCell(new Phrase(justificacion, new Font(Font.HELVETICA, 8))));
        }
        
        doc.add(table);
        
        // Footer
        doc.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Reporte generado por el Sistema de Asistencias - EduNova",
                                new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
        
        doc.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_clase_" + classId + "_" + date + ".pdf")
                .body(out.toByteArray());
    }
}
