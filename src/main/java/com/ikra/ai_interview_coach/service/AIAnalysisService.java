package com.ikra.ai_interview_coach.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class AIAnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    public String analyzeAnswer(String question, String answer) {

        try {
            String prompt = """
                    Sen profesyonel bir teknik mülakat değerlendiricisisin.

                    Aşağıdaki cevabı değerlendir.

                    Sadece geçerli JSON döndür. Markdown, açıklama veya kod bloğu kullanma.

                    Format tam olarak şu şekilde olsun:

                    {
                      "score": 85,
                      "feedback": "Teknik açıklama iyi ancak örnek eksik.",
                      "missingConcepts": ["getter/setter", "data hiding"],
                      "strengths": ["temel kavram doğru açıklanmış", "cevap anlaşılır"]
                    }

                    Soru:
                    %s

                    Cevap:
                    %s
                    """.formatted(question, answer);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 300
            );

            return webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> choices =
                                (List<Map<String, Object>>) response.get("choices");

                        Map<String, Object> message =
                                (Map<String, Object>) choices.get(0).get("message");

                        String content = message.get("content").toString();

                        content = content.replace("```json", "")
                                .replace("```", "")
                                .trim();

                        return content;
                    })
                    .block();

        } catch (WebClientResponseException.TooManyRequests e) {
            return "{\"score\":0,\"feedback\":\"OpenAI API isteği şu anda limit nedeniyle yanıt veremedi. Yerel analiz sonucu kullanılabilir.\",\"missingConcepts\":[],\"strengths\":[]}";
        } catch (WebClientResponseException.Unauthorized e) {
            return "{\"score\":0,\"feedback\":\"OpenAI API anahtarı geçersiz veya yetkisiz görünüyor. API key kontrol edilmelidir.\",\"missingConcepts\":[],\"strengths\":[]}";
        } catch (Exception e) {
            return "{\"score\":0,\"feedback\":\"AI değerlendirmesi şu anda alınamadı. Yerel analiz sistemi çalışmaya devam ediyor.\",\"missingConcepts\":[],\"strengths\":[]}";
        }
    }
}