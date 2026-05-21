package com.ikra.ai_interview_coach.controller;

import com.ikra.ai_interview_coach.service.AIAnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
@Controller
public class InterviewController {

    private final AIAnalysisService aiAnalysisService;

    public InterviewController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/question")
    public String getQuestion(@RequestParam String category, Model model) {

        Map<String, List<String>> questionMap = new HashMap<>();

        questionMap.put("java", Arrays.asList(
                "Java'da interface ve abstract class arasındaki fark nedir?",
                "Java'da encapsulation kavramı ne işe yarar?",
                "Polymorphism nedir?",
                "Exception handling neden önemlidir?",
                "JVM, JDK ve JRE farkı nedir?"
        ));

        questionMap.put("python", Arrays.asList(
                "Python'da list ve tuple farkı nedir?",
                "Decorator nedir?",
                "Python'da lambda function ne işe yarar?",
                "Python'da GIL nedir?",
                "Virtual environment neden kullanılır?"
        ));

        questionMap.put("ai", Arrays.asList(
                "Makine öğrenmesi ile derin öğrenme farkı nedir?",
                "Overfitting nedir?",
                "Neural network nasıl çalışır?",
                "Supervised learning nedir?",
                "NLP hangi alanlarda kullanılır?"
        ));

        questionMap.put("cyber", Arrays.asList(
                "SQL Injection nedir ve nasıl önlenir?",
                "XSS saldırısı nedir?",
                "Hashing ve encryption farkı nedir?",
                "Authentication ve authorization farkı nedir?",
                "Firewall ne işe yarar?"
        ));

        List<String> questions = questionMap.getOrDefault(category,
                Collections.singletonList("Genel bir teknik kavram açıklayınız."));

        Random random = new Random();
        String question = questions.get(random.nextInt(questions.size()));

        model.addAttribute("category", category);
        model.addAttribute("question", question);

        return "question";
    }

    @PostMapping("/analyze")
    public String analyzeAnswer(
            @RequestParam String question,
            @RequestParam String answer,
            @RequestParam String category,
            Model model) {

        int wordCount = answer.trim().split("\\s+").length;

        List<String> keywords = new ArrayList<>();

String lowerQuestion = question.toLowerCase();

if(lowerQuestion.contains("encapsulation")) {

    keywords = Arrays.asList(
        "encapsulation",
        "kapsülleme",
        "private",
        "getter",
        "setter",
        "veri gizleme",
        "erişim",
        "kontrol",
        "güvenlik"
    );

}
else if(lowerQuestion.contains("interface")) {

    keywords = Arrays.asList(
        "interface",
        "abstract",
        "class",
        "method",
        "inheritance",
        "polymorphism"
    );

}
else if(lowerQuestion.contains("sql injection")) {

    keywords = Arrays.asList(
        "sql",
        "injection",
        "prepared statement",
        "validation",
        "security"
    );

}

        else if (category.equals("python")) {
            keywords = Arrays.asList(
                    "list",
                    "tuple",
                    "dictionary",
                    "decorator",
                    "lambda",
                    "function"
            );
        }

        else if (category.equals("ai")) {
            keywords = Arrays.asList(
                    "machine learning",
                    "deep learning",
                    "neural network",
                    "dataset",
                    "model",
                    "training"
            );
        }

        else if (category.equals("cyber")) {
            keywords = Arrays.asList(
                    "sql",
                    "injection",
                    "prepared statement",
                    "validation",
                    "authentication",
                    "authorization"
            );
        }
        else if(lowerQuestion.contains("supervised")) {

    keywords = Arrays.asList(
        "supervised",
        "denetimli",
        "etiketli",
        "label",
        "model",
        "eğitim",
        "tahmin",
        "input",
        "output"
    );

}

        int keywordCount = 0;
        List<String> missingKeywords = new ArrayList<>();

        for (String keyword : keywords) {

            if (answer.toLowerCase().contains(keyword.toLowerCase())) {
                keywordCount++;
            } else {
                missingKeywords.add(keyword);
            }
        }

       int keywordScore = 0;

if (!keywords.isEmpty()) {
    keywordScore = (keywordCount * 100) / keywords.size();
    keywordScore = keywordScore / 2;
}

        int lengthScore = 0;

        if (wordCount >= 80) {
            lengthScore = 25;
        } else if (wordCount >= 50) {
            lengthScore = 15;
        } else if (wordCount >= 30) {
            lengthScore = 10;
        }

        int explanationScore = 0;

        if (
        answer.toLowerCase().contains("çünkü") ||
        answer.toLowerCase().contains("örneğin") ||
        answer.toLowerCase().contains("mesela") ||
        answer.toLowerCase().contains("bu nedenle") ||
        answer.toLowerCase().contains("sayesinde") ||
        answer.toLowerCase().contains("güvenlik") ||
        answer.toLowerCase().contains("kontrollü erişim") ||
        answer.toLowerCase().contains("veri gizleme") ||
        answer.toLowerCase().contains("koruma")
) {
            explanationScore = 20;
        } else if (wordCount > 40) {
            explanationScore = 10;
        }

        int totalScore = keywordScore + lengthScore + explanationScore;

        if (totalScore > 100) {
            totalScore = 100;
        }

        String feedback;

        if (totalScore >= 85) {
            feedback = "Cevabın oldukça güçlü. Teknik açıklaman başarılı ve detay seviyesi yüksek.";
        }

        else if (totalScore >= 60) {
            feedback = "Cevabın iyi seviyede. Bazı kavramları veya örnekleri biraz daha açabilirsin.";
        }

        else {
            feedback = "Cevabında teknik kavramlar var ancak açıklama kalitesi düşük. Örnek, neden-sonuç veya kullanım amacı ekleyebilirsin.";
        }

        String aiFeedback = aiAnalysisService.analyzeAnswer(question, answer);
        String aiScore = "0";
String aiTextFeedback = aiFeedback;
String aiMissingConcepts = "[]";
String aiStrengths = "[]";

try {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(aiFeedback);

    aiScore = node.get("score").asText();
    aiTextFeedback = node.get("feedback").asText();
    aiMissingConcepts = node.get("missingConcepts").toString();
    aiStrengths = node.get("strengths").toString();

} catch (Exception e) {
    aiTextFeedback = aiFeedback;
}

      String suggestedAnswer;

if (question.toLowerCase().contains("encapsulation")) {

    suggestedAnswer =
            "Encapsulation, Java'da sınıf içindeki verileri dış erişime karşı koruma prensibidir. " +
            "Değişkenler private tanımlanır ve bu verilere getter ve setter metotları ile kontrollü erişim sağlanır. " +
            "Bu sayede veri gizleme, güvenlik ve kodun sürdürülebilirliği artar.";

}

else if (question.toLowerCase().contains("interface")) {

    suggestedAnswer =
            "Interface, sınıflara belirli davranışları kazandırmak için kullanılan bir sözleşme yapısıdır. " +
            "Abstract class ise hem ortak özellikleri hem de bazı metot gövdelerini içerebilir. " +
            "Interface çoklu davranış desteği sağlarken, abstract class daha çok ortak yapı ve kalıtım ilişkisi kurmak için kullanılır.";

}

else if (question.toLowerCase().contains("polymorphism")) {

    suggestedAnswer =
            "Polymorphism, nesne yönelimli programlamada aynı metodun farklı sınıflarda farklı davranış gösterebilmesidir. " +
            "Genellikle inheritance ve method overriding ile sağlanır. " +
            "Bu yapı kodun esnekliğini ve yeniden kullanılabilirliğini artırır.";

}

else if (question.toLowerCase().contains("abstraction")) {

    suggestedAnswer =
            "Abstraction, gereksiz detayları gizleyip yalnızca gerekli işlevleri kullanıcıya sunma prensibidir. " +
            "Java'da abstract class ve interface yapıları ile uygulanabilir. " +
            "Bu sayede karmaşıklık azaltılır ve daha sürdürülebilir kod yazılır.";

}

else {

    suggestedAnswer =
            "Bu soruya güçlü bir cevap verirken kavramı tanımlamalı, kullanım amacını açıklamalı ve kısa bir örnekle desteklemelisin.";

}

        model.addAttribute("score", totalScore);
        model.addAttribute("feedback", feedback);
        model.addAttribute("aiFeedback", aiFeedback);

        model.addAttribute("keywordSuccess", keywordScore);
        model.addAttribute("lengthSuccess", lengthScore);
        model.addAttribute("explanationSuccess", explanationScore);

        model.addAttribute("wordCount", wordCount);
        model.addAttribute("missingKeywords", missingKeywords);
        model.addAttribute("aiScore", aiScore);
model.addAttribute("aiTextFeedback", aiTextFeedback);
model.addAttribute("aiMissingConcepts", aiMissingConcepts);
model.addAttribute("aiStrengths", aiStrengths);

        model.addAttribute("suggestedAnswer", suggestedAnswer);

        return "result";
    }
}