package com.financetracker.service;

import com.financetracker.dto.CategoryAmount;
import com.financetracker.dto.FinanceSummary;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the monthly report: a deterministic summary via FinanceAnalyzerService,
 * plus an optional server-rendered PDF export streamed straight to the response
 * (capped at 40 line items to keep the download small and fast).
 */
@Service
public class ReportService {

    private final FinanceAnalyzerService financeAnalyzerService;

    public ReportService(FinanceAnalyzerService financeAnalyzerService) {
        this.financeAnalyzerService = financeAnalyzerService;
    }

    public FinanceSummary buildMonthlySummary(List<Transaction> monthTransactions, Budget budget) {
        return financeAnalyzerService.analyze(monthTransactions, budget);
    }

    public byte[] buildPdf(int year, int month, List<Transaction> monthTransactions, FinanceSummary summary) {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font headingFont = new Font(Font.HELVETICA, 13, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

            String monthLabel = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            Paragraph title = new Paragraph("Finance Report -- " + monthLabel, titleFont);
            title.setSpacingAfter(15);
            document.add(title);

            // Summary cards
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);
            addSummaryCell(summaryTable, "Total Income", currency(summary.getTotalIncome()), boldFont, normalFont);
            addSummaryCell(summaryTable, "Total Expense", currency(summary.getTotalExpense()), boldFont, normalFont);
            addSummaryCell(summaryTable, "Savings", currency(summary.getSavings()), boldFont, normalFont);
            addSummaryCell(summaryTable, "Budget Remaining", currency(summary.getBudgetRemaining()), boldFont, normalFont);
            document.add(summaryTable);

            // Category breakdown
            document.add(new Paragraph("Category Breakdown", headingFont));
            PdfPTable catTable = new PdfPTable(2);
            catTable.setWidthPercentage(60);
            catTable.setSpacingBefore(8);
            catTable.setSpacingAfter(20);
            catTable.addCell(cell("Category", boldFont));
            catTable.addCell(cell("Amount", boldFont));
            for (CategoryAmount ca : summary.getCategoryBreakdown()) {
                catTable.addCell(cell(ca.getCategory(), normalFont));
                catTable.addCell(cell(currency(ca.getAmount()), normalFont));
            }
            document.add(catTable);

            // Transactions (capped at 40 to keep the PDF light)
            document.add(new Paragraph("Transactions", headingFont));
            PdfPTable txTable = new PdfPTable(5);
            txTable.setWidthPercentage(100);
            txTable.setSpacingBefore(8);
            txTable.setWidths(new float[]{2, 3, 2, 2, 2});
            for (String h : new String[]{"Date", "Title", "Category", "Type", "Amount"}) {
                txTable.addCell(cell(h, boldFont));
            }
            List<Transaction> capped = monthTransactions.stream().limit(40).collect(Collectors.toList());
            for (Transaction t : capped) {
                txTable.addCell(cell(t.getTransactionDate().toString(), normalFont));
                txTable.addCell(cell(t.getTitle(), normalFont));
                txTable.addCell(cell(t.getCategory(), normalFont));
                txTable.addCell(cell(t.getType().name(), normalFont));
                txTable.addCell(cell(currency(t.getAmount()), normalFont));
            }
            document.add(txTable);

            if (monthTransactions.size() > 40) {
                Paragraph note = new Paragraph(
                        "Showing the first 40 of " + monthTransactions.size() + " transactions. Full data is available in the app.",
                        new Font(Font.HELVETICA, 8, Font.ITALIC));
                note.setSpacingBefore(10);
                document.add(note);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addSummaryCell(PdfPTable table, String label, String value, Font boldFont, Font normalFont) {
        PdfPTable inner = new PdfPTable(1);
        inner.addCell(cell(label, normalFont));
        inner.addCell(cell(value, boldFont));
        table.addCell(inner);
    }

    private com.lowagie.text.pdf.PdfPCell cell(String text, Font font) {
        var cell = new com.lowagie.text.pdf.PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }

    private String currency(java.math.BigDecimal value) {
        if (value == null) value = java.math.BigDecimal.ZERO;
        return "Rs. " + value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
