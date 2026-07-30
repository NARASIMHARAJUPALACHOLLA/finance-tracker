package com.financetracker.dto;

import java.util.List;

public class InsightsResponse {
    private List<String> insights;
    private List<String> recommendations;
    private String provider; // "openai" or "heuristic"

    public InsightsResponse(List<String> insights, List<String> recommendations, String provider) {
        this.insights = insights;
        this.recommendations = recommendations;
        this.provider = provider;
    }

    public List<String> getInsights() { return insights; }
    public List<String> getRecommendations() { return recommendations; }
    public String getProvider() { return provider; }
}
