package com.vg.attendance.infrastructure.adapter.in.web.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import com.vg.attendance.infrastructure.adapter.out.client.ScheduleClient;
import com.vg.attendance.infrastructure.adapter.out.client.UserClient;
import com.vg.attendance.infrastructure.adapter.out.client.dto.ScheduleResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api/attendance/reports")
public class ReportController {

    private final AttendanceRepositoryPort attendanceRepository;
    private final UserClient userClient;
    private final ScheduleClient scheduleClient;

    @Value("${school.name:INSTITUCIÓN EDUCATIVA PÚBLICA MIXTO SAN LUIS}")
    private String schoolName;

    @Value("${school.modular-code:0286427}")
    private String modularCode;

    @Value("${school.local-code:354416}")
    private String localCode;

    public ReportController(AttendanceRepositoryPort attendanceRepository, 
                            UserClient userClient,
                            ScheduleClient scheduleClient) {
        this.attendanceRepository = attendanceRepository;
        this.userClient = userClient;
        this.scheduleClient = scheduleClient;
    }

    @GetMapping("/class/{classId}")
    public Mono<ResponseEntity<byte[]>> getClassReport(
            @PathVariable String classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Reporte por clase: {}, fecha: {}", classId, date);
        String token = authHeader.substring(7);

        return attendanceRepository.findByClaseIdAndFecha(classId, date)
            .collectList()
            .flatMap(attendances -> {
                if (attendances.isEmpty()) {
                    return generatePdf(() -> generarPdfError("No hay asistencias para esta clase en la fecha seleccionada"));
                }
                return obtenerNombres(attendances, token)
                    .flatMap(nombres -> generatePdf(() -> generarPdfClase(attendances, date, classId, nombres)));
            })
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return generatePdf(() -> generarPdfError("Error generando reporte: " + e.getMessage()));
            });
    }

    @GetMapping("/student/{studentId}")
    public Mono<ResponseEntity<byte[]>> getStudentReport(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Reporte por estudiante: {} desde {} hasta {}", studentId, startDate, endDate);
        String token = authHeader.substring(7);

        return attendanceRepository.findByEstudianteIdAndFechaBetween(studentId, startDate, endDate)
            .collectList()
            .flatMap(attendances -> {
                if (attendances.isEmpty()) {
                    return generatePdf(() -> generarPdfError("No hay asistencias para este estudiante en el período seleccionado"));
                }
                return Mono.zip(
                    obtenerNombreEstudiante(studentId, token),
                    obtenerNombres(attendances, token)
                ).flatMap(tuple -> {
                    String nombre = tuple.getT1();
                    Map<String, String> nombres = tuple.getT2();
                    return generatePdf(() -> generarPdfEstudiante(attendances, studentId, nombre, startDate, endDate, nombres));
                });
            })
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return generatePdf(() -> generarPdfError("Error generando reporte: " + e.getMessage()));
            });
    }

    @GetMapping("/daily")
    public Mono<ResponseEntity<byte[]>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Reporte diario: {}", date);
        String token = authHeader.substring(7);

        return attendanceRepository.findByFecha(date)
            .collectList()
            .flatMap(attendances -> {
                if (attendances.isEmpty()) {
                    return generatePdf(() -> generarPdfError("No hay asistencias registradas para esta fecha"));
                }
                return obtenerNombres(attendances, token)
                    .flatMap(nombres -> generatePdf(() -> generarPdfDiario(attendances, date, nombres)));
            })
            .onErrorResume(e -> {
                log.error("Error: {}", e.getMessage());
                return generatePdf(() -> generarPdfError("Error generando reporte: " + e.getMessage()));
            });
    }

    private Mono<ResponseEntity<byte[]>> generatePdf(Supplier<ResponseEntity<byte[]>> generator) {
        return Mono.fromCallable(generator::get)
            .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Map<String, String>> obtenerNombres(List<Attendance> attendances, String token) {
        Map<String, String> nombres = new ConcurrentHashMap<>();
        
        // Obtener todos los IDs únicos de estudiantes y profesores
        List<String> userIds = attendances.stream()
            .flatMap(a -> java.util.stream.Stream.of(a.getEstudianteId(), a.getProfesorId()))
            .distinct()
            .collect(Collectors.toList());

        return Mono.zip(
            userIds.stream()
                .map(id -> userClient.getUserById(id, token)
                    .map(user -> Map.entry(id, user.getFirstName() + " " + user.getLastName()))
                    .defaultIfEmpty(Map.entry(id, id)))
                .collect(Collectors.toList()),
            results -> {
                for (Object result : results) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) result;
                    nombres.put(entry.getKey(), entry.getValue());
                }
                return nombres;
            }
        );
    }

    private Mono<String> obtenerNombreEstudiante(String studentId, String token) {
        return userClient.getUserById(studentId, token)
            .map(user -> user.getFirstName() + " " + user.getLastName())
            .defaultIfEmpty(studentId);
    }

    private ResponseEntity<byte[]> generarPdfError(String mensaje) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(mensaje));
            doc.close();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error.pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> generarPdfClase(List<Attendance> attendances, LocalDate date, 
                                                    String classId, Map<String, String> nombres) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{20f, 80f});

            try {
                Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
                logo.scaleToFit(70, 70);
                PdfPCell logocell = new PdfPCell(logo);
                logocell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(logocell);
            } catch (Exception e) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(emptyCell);
            }

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            Paragraph title = new Paragraph(schoolName, new Font(Font.HELVETICA, 14, Font.BOLD));
            title.setAlignment(Element.ALIGN_LEFT);
            titleCell.addElement(title);
            titleCell.addElement(new Paragraph("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                                    new Font(Font.HELVETICA, 9, Font.NORMAL)));
            headerTable.addCell(titleCell);
            doc.add(headerTable);
            doc.add(new Paragraph(" "));

            Paragraph titulo = new Paragraph("REPORTE DE ASISTENCIA POR CLASE", new Font(Font.HELVETICA, 16, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            
            doc.add(new Paragraph("Clase ID: " + classId + " - Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
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
                PdfPCell cell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0,51,102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                stats.addCell(cell);
            }
            
            for (int i = 0; i < values.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(values[i]), new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(colors[i]);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(10);
                stats.addCell(cell);
            }
            doc.add(stats);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{8f, 37f, 15f, 12f, 28f});

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
                
                String nombre = nombres.getOrDefault(a.getEstudianteId(), a.getEstudianteId());
                if (nombre.length() > 30) nombre = nombre.substring(0, 27) + "...";
                
                table.addCell(new PdfPCell(new Phrase(String.valueOf(counter++), new Font(Font.HELVETICA, 9))));
                table.addCell(new PdfPCell(new Phrase(nombre, new Font(Font.HELVETICA, 9))));
                
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
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_clase_" + classId + "_" + date + ".pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> generarPdfEstudiante(List<Attendance> attendances, String studentId, 
                                                         String nombreEstudiante, LocalDate startDate, 
                                                         LocalDate endDate, Map<String, String> nombres) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{20f, 80f});

            try {
                Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
                logo.scaleToFit(70, 70);
                PdfPCell logocell = new PdfPCell(logo);
                logocell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(logocell);
            } catch (Exception e) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(emptyCell);
            }

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            titleCell.addElement(new Paragraph(schoolName, new Font(Font.HELVETICA, 14, Font.BOLD)));
            titleCell.addElement(new Paragraph("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                                    new Font(Font.HELVETICA, 9, Font.NORMAL)));
            headerTable.addCell(titleCell);
            doc.add(headerTable);
            doc.add(new Paragraph(" "));

            Paragraph titulo = new Paragraph("REPORTE DE ASISTENCIA POR ESTUDIANTE", new Font(Font.HELVETICA, 16, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            
            doc.add(new Paragraph("Estudiante: " + nombreEstudiante));
            doc.add(new Paragraph("Período: " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                  " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
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
                PdfPCell cell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0,51,102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                stats.addCell(cell);
            }
            
            for (int i = 0; i < values.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(values[i]), new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(colors[i]);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(10);
                stats.addCell(cell);
            }
            doc.add(stats);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{12f, 25f, 12f, 12f, 39f});

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

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_estudiante_" + studentId + ".pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> generarPdfDiario(List<Attendance> attendances, LocalDate date, Map<String, String> nombres) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{20f, 80f});

            try {
                Image logo = Image.getInstance(new ClassPathResource("templates/images/logomxt.png").getInputStream().readAllBytes());
                logo.scaleToFit(70, 70);
                PdfPCell logocell = new PdfPCell(logo);
                logocell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(logocell);
            } catch (Exception e) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerTable.addCell(emptyCell);
            }

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            titleCell.addElement(new Paragraph(schoolName, new Font(Font.HELVETICA, 14, Font.BOLD)));
            titleCell.addElement(new Paragraph("Código Modular: " + modularCode + " - Código Local: " + localCode, 
                                    new Font(Font.HELVETICA, 9, Font.NORMAL)));
            headerTable.addCell(titleCell);
            doc.add(headerTable);
            doc.add(new Paragraph(" "));

            Paragraph titulo = new Paragraph("REPORTE DIARIO DE ASISTENCIA", new Font(Font.HELVETICA, 16, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            doc.add(new Paragraph("Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            doc.add(new Paragraph(" "));

            Map<String, List<Attendance>> porClase = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getClaseId));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10f, 30f, 15f, 12f, 28f, 15f});

            String[] headers = {"Clase", "Estudiante", "Estado", "Hora", "Justificación", "Profesor"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                cell.setBackgroundColor(new Color(0, 51, 102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (Map.Entry<String, List<Attendance>> entry : porClase.entrySet()) {
                String claseId = entry.getKey();
                List<Attendance> asistencias = entry.getValue();
                
                for (int i = 0; i < asistencias.size(); i++) {
                    Attendance a = asistencias.get(i);
                    
                    String estado = "";
                    Color estadoColor = Color.BLACK;
                    switch (a.getEstado()) {
                        case "A": estado = "ASISTIÓ"; estadoColor = new Color(40, 167, 69); break;
                        case "T": estado = "TARDANZA"; estadoColor = new Color(255, 193, 7); break;
                        case "J": estado = "JUSTIFICADO"; estadoColor = new Color(0, 123, 255); break;
                        case "F": estado = "FALTÓ"; estadoColor = new Color(220, 53, 69); break;
                        default: estado = a.getEstado();
                    }
                    
                    String nombreEstudiante = nombres.getOrDefault(a.getEstudianteId(), a.getEstudianteId());
                    String nombreProfesor = nombres.getOrDefault(a.getProfesorId(), a.getProfesorId());
                    
                    if (nombreProfesor.length() > 25) nombreProfesor = nombreProfesor.substring(0, 22) + "...";
                    
                    if (i == 0) {
                        PdfPCell claseCell = new PdfPCell(new Phrase(claseId, new Font(Font.HELVETICA, 9, Font.BOLD)));
                        claseCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        claseCell.setRowspan(asistencias.size());
                        table.addCell(claseCell);
                    }
                    
                    table.addCell(new PdfPCell(new Phrase(nombreEstudiante, new Font(Font.HELVETICA, 9))));
                    
                    PdfPCell estadoCell = new PdfPCell(new Phrase(estado, new Font(Font.HELVETICA, 9, Font.BOLD, estadoColor)));
                    estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(estadoCell);
                    
                    String hora = a.getHoraLlegada() != null ? a.getHoraLlegada().toString() : "-";
                    PdfPCell horaCell = new PdfPCell(new Phrase(hora, new Font(Font.HELVETICA, 9)));
                    horaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(horaCell);
                    
                    String justificacion = a.getJustificacionNota() != null ? a.getJustificacionNota() : "-";
                    if (justificacion.length() > 25) justificacion = justificacion.substring(0, 22) + "...";
                    table.addCell(new PdfPCell(new Phrase(justificacion, new Font(Font.HELVETICA, 8))));
                    
                    table.addCell(new PdfPCell(new Phrase(nombreProfesor, new Font(Font.HELVETICA, 8))));
                }
            }
            
            doc.add(table);
            
            Paragraph footer = new Paragraph("Reporte generado por el Sistema de Asistencias - EduNova",
                                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);
            
            doc.close();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_diario_" + date + ".pdf")
                    .body(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
