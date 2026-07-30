package com.financetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.FinanceSummary;
import com.financetracker.dto.InsightsResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Wraps the OpenAI Chat Completions API. analyzeFinance() (FinanceAnalyzerService)
 * runs first and always succeeds -- its insights/recommendations are the
 * deterministic fallback. When OPENAI_API_KEY is configured, the summary
 * numbers (never raw transactions) are sent to gpt-4o-mini for a sharper,
 * natural-language read. Any failure -- missing key, network error, bad JSON --
 * is swallowed and the heuristic output is returned instead, so this feature
 * always works.
 */
@Service
public class AIService {

    private final FinanceAnalyzerService financeAnalyzerService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.openai.base-url}")
    private String baseUrl;

    public AIService(FinanceAnalyzerService financeAnalyzerService, RestTemplate restTemplate) {
        this.financeAnalyzerService = financeAnalyzerService;
        this.restTemplate = restTemplate;
    }

    private boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("your_openai_api_key_here");
    }

    public InsightsResponse generateInsights(List<Transaction> transactions, Budget budget) {
        FinanceSummary analysis = financeAnalyzerService.analyze(transactions, budget);

        if (!hasApiKey()) {
            return new InsightsResponse(analysis.getInsights(), analysis.getRecommendations(), "heuristic");
        }

        try {
            List<String> aiInsights = callOpenAI(analysis);
            return new InsightsResponse(aiInsights, analysis.getRecommendations(), "openai");
        } catch (Exception e) {
            // Network issue, rate limit, or bad response -- fall back silently.
            return new InsightsResponse(analysis.getInsights(), analysis.getRecommendations(), "heuristic");
        }
    }

    public FinanceSummary predictExpense(List<Transaction> transactions, Budget budget) {
        // Prediction is entirely local -- no external call needed.
        return financeAnalyzerService.analyze(transactions, budget);
    }

    private List<String> callOpenAI(FinanceSummary analysis) throws Exception {
        String prompt = buildPrompt(analysis);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "You are a concise personal-finance assistant. Return only a JSON array of strings, no other text."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String responseJson = restTemplate.postForObject(baseUrl, request, String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        String content = root.path("choices").path(0).path("message").path("content").asText();

        JsonNode arrayNode = objectMapper.readTree(content);
        return objectMapper.convertValue(arrayNode, List.class);
    }

    private String buildPrompt(FinanceSummary s) {
        // Only summary numbers are sent -- never individual transactions --
        // keeping the request short, cheap, and free of line-item data.
        return """
            Here is a user's personal finance summary:
            Total income: %s
            Total expense: %s
            Savings: %s
            Predicted next-period expense: %s
            Top spending categories: %s

            Write 3-5 short, specific, encouraging insight sentences about their finances.
            Return only a JSON array of strings.
            """.formatted(
                s.getTotalIncome(), s.getTotalExpense(), s.getSavings(), s.getPredictedExpense(),
                s.getCategoryBreakdown()
        );
    }
}
