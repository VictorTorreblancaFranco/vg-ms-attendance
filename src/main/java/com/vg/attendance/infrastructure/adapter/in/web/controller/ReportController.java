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
import org.springframework.http.HttpStatus;
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
            .map(attendances -> generarPdfClase(attendances, date, classId))
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @GetMapping("/student/{studentId}")
    public Mono<ResponseEntity<byte[]>> getStudentReport(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Generando reporte para estudiante: {} desde {} hasta {}", studentId, startDate, endDate);

        return attendanceRepository.findByEstudianteIdAndFechaBetween(studentId, startDate, endDate)
            .collectList()
            .map(attendances -> generarPdfEstudiante(attendances, studentId, startDate, endDate))
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    private ResponseEntity<byte[]> generarPdfClase(List<Attendance> attendances, LocalDate date, String classId) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Logo
            try {
                Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
                logo.scaleToFit(60, 60);
                doc.add(logo);
            } catch (Exception e) {
                log.warn("Logo no encontrado");
            }

            Paragraph school = new Paragraph(schoolName, new Font(Font.HELVETICA, 12, Font.BOLD));
            school.setAlignment(Element.ALIGN_CENTER);
            doc.add(school);
            
            Paragraph codes = new Paragraph("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                                            new Font(Font.HELVETICA, 9, Font.NORMAL));
            codes.setAlignment(Element.ALIGN_CENTER);
            doc.add(codes);
            
            doc.add(new Paragraph(" "));

            Paragraph title = new Paragraph("REPORTE DE ASISTENCIA POR CLASE", new Font(Font.HELVETICA, 14, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            
            Paragraph info = new Paragraph("Clase: " + classId + " - Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                            new Font(Font.HELVETICA, 10, Font.NORMAL));
            info.setAlignment(Element.ALIGN_CENTER);
            doc.add(info);
            doc.add(new Paragraph(" "));

            long total = attendances.size();
            long asistieron = attendances.stream().filter(a -> "A".equals(a.getEstado())).count();
            long tardanza = attendances.stream().filter(a -> "T".equals(a.getEstado())).count();
            long justificados = attendances.stream().filter(a -> "J".equals(a.getEstado())).count();
            long faltaron = attendances.stream().filter(a -> "F".equals(a.getEstado())).count();

            PdfPTable stats = new PdfPTable(5);
            stats.setWidthPercentage(100);
            
            String[] labels = {"Total", "Asistieron", "Tardanza", "Justificados", "Faltaron"};
            long[] values = {total, asistieron, tardanza, justificados, faltaron};
            Color[] colors = {new Color(0,51,102), new Color(40,167,69), new Color(255,193,7), new Color(0,123,255), new Color(220,53,69)};
            
            for (String label : labels) {
                PdfPCell cell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0,51,102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                stats.addCell(cell);
            }
            
            for (int i = 0; i < values.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(values[i]), new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(colors[i]);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                stats.addCell(cell);
            }
            doc.add(stats);
            doc.add(new Paragraph(" "));

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
            
            Paragraph footer = new Paragraph("Reporte generado por el Sistema de Asistencias - EduNova",
                                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);
            
            doc.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_clase_" + classId + "_" + date + ".pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> generarPdfEstudiante(List<Attendance> attendances, String studentId, LocalDate startDate, LocalDate endDate) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            try {
                Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
                logo.scaleToFit(60, 60);
                logo.setAlignment(Element.ALIGN_LEFT);
                doc.add(logo);
            } catch (Exception e) {
                log.warn("Logo no encontrado");
            }

            Paragraph school = new Paragraph(schoolName, new Font(Font.HELVETICA, 12, Font.BOLD));
            school.setAlignment(Element.ALIGN_CENTER);
            doc.add(school);
            
            Paragraph codes = new Paragraph("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                                            new Font(Font.HELVETICA, 9, Font.NORMAL));
            codes.setAlignment(Element.ALIGN_CENTER);
            doc.add(codes);
            doc.add(new Paragraph(" "));

            Paragraph title = new Paragraph("REPORTE DE ASISTENCIA POR ESTUDIANTE", new Font(Font.HELVETICA, 14, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            
            Paragraph student = new Paragraph("Estudiante ID: " + studentId, new Font(Font.HELVETICA, 10, Font.NORMAL));
            student.setAlignment(Element.ALIGN_CENTER);
            doc.add(student);
            
            Paragraph periodo = new Paragraph("Período: " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                              " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                              new Font(Font.HELVETICA, 10, Font.NORMAL));
            periodo.setAlignment(Element.ALIGN_CENTER);
            doc.add(periodo);
            doc.add(new Paragraph(" "));

            long total = attendances.size();
            long asistieron = attendances.stream().filter(a -> "A".equals(a.getEstado())).count();
            long tardanza = attendances.stream().filter(a -> "T".equals(a.getEstado())).count();
            long justificados = attendances.stream().filter(a -> "J".equals(a.getEstado())).count();
            long faltaron = attendances.stream().filter(a -> "F".equals(a.getEstado())).count();

            PdfPTable stats = new PdfPTable(5);
            stats.setWidthPercentage(100);
            
            String[] labels = {"Total Clases", "Asistió", "Tardanza", "Justificado", "Faltó"};
            long[] values = {total, asistieron, tardanza, justificados, faltaron};
            Color[] colors = {new Color(0,51,102), new Color(40,167,69), new Color(255,193,7), new Color(0,123,255), new Color(220,53,69)};
            
            for (String label : labels) {
                PdfPCell cell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0,51,102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                stats.addCell(cell);
            }
            
            for (int i = 0; i < values.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(values[i]), new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(colors[i]);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                stats.addCell(cell);
            }
            doc.add(stats);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{12f, 20f, 12f, 12f, 44f});

            String[] headers = {"Fecha", "Clase", "Estado", "Hora", "Justificación"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0, 51, 102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

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
                
                table.addCell(new PdfPCell(new Phrase(a.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 9))));
                table.addCell(new PdfPCell(new Phrase(a.getClaseId(), new Font(Font.HELVETICA, 9))));
                
                PdfPCell estadoCell = new PdfPCell(new Phrase(estado, new Font(Font.HELVETICA, 9, Font.BOLD, estadoColor)));
                estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(estadoCell);
                
                String hora = a.getHoraLlegada() != null ? a.getHoraLlegada().toString() : "-";
                PdfPCell horaCell = new PdfPCell(new Phrase(hora, new Font(Font.HELVETICA, 9)));
                horaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(horaCell);
                
                String justificacion = a.getJustificacionNota() != null ? a.getJustificacionNota() : "-";
                if (justificacion.length() > 40) justificacion = justificacion.substring(0, 37) + "...";
                table.addCell(new PdfPCell(new Phrase(justificacion, new Font(Font.HELVETICA, 8))));
            }
            doc.add(table);
            
            Paragraph footer = new Paragraph("Reporte generado por el Sistema de Asistencias - EduNova",
                                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);
            
            doc.close();

            String shortId = studentId.length() > 8 ? studentId.substring(studentId.length() - 8) : studentId;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_estudiante_" + shortId + ".pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
