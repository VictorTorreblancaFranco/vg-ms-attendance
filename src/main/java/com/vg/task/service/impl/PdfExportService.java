package com.vg.task.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    public Mono<byte[]> exportTranscript(Integer studentId, String studentName) {
        return submissionRepository.findByStudentId(studentId)
            .collectList()
            .flatMap(submissions -> {
                if (submissions.isEmpty()) {
                    return Mono.error(new RuntimeException("No hay entregas para el estudiante"));
                }
                return taskRepository.findAll()
                    .filter(task -> submissions.stream().anyMatch(s -> s.getTaskId().equals(task.getId())))
                    .collectList()
                    .map(tasks -> generatePdf(studentId, studentName, submissions, tasks));
            });
    }

    private byte[] generatePdf(Integer studentId, String studentName, List<Submission> submissions, List<Task> tasks) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            document.add(new Paragraph("BOLETA DE NOTAS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
            
            document.add(new Paragraph("Estudiante: " + studentName + " (ID: " + studentId + ")")
                .setMarginTop(20));
            
            double average = submissions.stream()
                .filter(s -> s.getGrade() != null)
                .mapToDouble(Submission::getGrade)
                .average()
                .orElse(0.0);
            
            document.add(new Paragraph("Promedio General: " + String.format("%.2f", average))
                .setMarginBottom(20));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 4, 2, 2, 3}))
                .useAllAvailableWidth();
            
            String[] headers = {"Tarea", "Descripción", "Nota", "Presentó", "Observación"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold());
                cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                table.addCell(cell);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (Submission sub : submissions) {
                Task task = tasks.stream().filter(t -> t.getId().equals(sub.getTaskId())).findFirst().orElse(null);
                table.addCell(task != null ? task.getTitle() : "N/A");
                table.addCell(task != null && task.getDescription() != null ? task.getDescription() : "-");
                table.addCell(sub.getGrade() != null ? String.format("%.2f", sub.getGrade()) : "-");
                table.addCell(sub.getPresented() != null && sub.getPresented() ? "Sí" : "No");
                table.addCell(sub.getObservations() != null && sub.getObservations().length() > 30 ? 
                    sub.getObservations().substring(0, 27) + "..." : (sub.getObservations() != null ? sub.getObservations() : "-"));
            }
            
            document.add(table);
            document.add(new Paragraph("\nFecha de emisión: " + java.time.LocalDate.now())
                .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
