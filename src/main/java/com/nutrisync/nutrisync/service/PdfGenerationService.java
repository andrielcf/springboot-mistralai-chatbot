package com.nutrisync.nutrisync.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nutrisync.nutrisync.entity.DayPlan;
import com.nutrisync.nutrisync.entity.DietPlan;
import com.nutrisync.nutrisync.entity.Meal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    public byte[] generateDietPlanPdf(DietPlan dietPlan) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 30, 30);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        addTitle(document);
        addMessage(document, dietPlan.getMessage());
        addWeeklyPlan(document, dietPlan.getWeeklyPlan());

        document.close();
        return outputStream.toByteArray();
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Plano Alimentar Personalizado", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void addMessage(Document document, String message) throws DocumentException {
        if (message != null && !message.isEmpty()) {
            Font messageFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph paragraph = new Paragraph(message, messageFont);
            paragraph.setSpacingAfter(15);
            document.add(paragraph);
        }
    }

    private void addWeeklyPlan(Document document, List<DayPlan> weeklyPlan) throws DocumentException {
        for (DayPlan dayPlan : weeklyPlan) {
            addDayTitle(document, dayPlan.getDay());
            addMealsTable(document, dayPlan.getPlanDay());
        }
    }

    private void addDayTitle(Document document, int day) throws DocumentException {
        Font dayFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE);
        Paragraph dayTitle = new Paragraph(getDayName(day), dayFont);
        dayTitle.setSpacingBefore(15);
        dayTitle.setSpacingAfter(10);
        document.add(dayTitle);
    }

    private void addMealsTable(Document document, List<Meal> meals) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        // Cabeçalho
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        addTableHeaderCell(table, "Refeição", headerFont, BaseColor.GRAY);
        addTableHeaderCell(table, "Horário", headerFont, BaseColor.GRAY);
        addTableHeaderCell(table, "Descrição", headerFont, BaseColor.GRAY);

        // Conteúdo
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        for (Meal meal : meals) {
            table.addCell(createCell(meal.getMealName(), contentFont));
            table.addCell(createCell(meal.getTime(), contentFont));
            table.addCell(createCell(meal.getDescription(), contentFont));
        }

        document.add(table);
    }

    private void addTableHeaderCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private String getDayName(int day) {
        return switch (day) {
            case 0 -> "Segunda-feira";
            case 1 -> "Terça-feira";
            case 2 -> "Quarta-feira";
            case 3 -> "Quinta-feira";
            case 4 -> "Sexta-feira";
            case 5 -> "Sábado";
            case 6 -> "Domingo";
            default -> "Dia " + (day + 1);
        };
    }

    private void addNutritionalInfo(Document document, DietPlan dietPlan) throws DocumentException {
        if (dietPlan.getMessage() != null && dietPlan.getMessage().contains("nutricionais")) {
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
            Paragraph info = new Paragraph("Informações nutricionais calculadas com base nas refeições sugeridas",
                    infoFont);
            info.setSpacingBefore(10);
            document.add(info);
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Paragraph footer = new Paragraph(
                "Gerado por NutriSync - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    // ... métodos auxiliares ...
}
