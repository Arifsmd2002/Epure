package com.project2.service;

import com.project2.entity.Product;
import com.project2.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomAnalyzerService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Autowired
    private ProductRepository productRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Analyzes a room image and returns a structured JSON recommendation string
     * from Gemini Vision API, cross-referenced with Epure's product catalog.
     */
    public String analyzeRoom(MultipartFile imageFile) throws IOException {
        // 1. Convert image to Base64
        byte[] imageBytes = imageFile.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";

        // 2. Build Epure product catalog context for the prompt
        String catalogContext = buildCatalogContext();

        // 3. Build the structured prompt
        String prompt = buildPrompt(catalogContext);

        // 4. Call Gemini Vision API
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            // Return a rich demo response if no API key is configured
            return buildDemoResponse();
        }

        return callGeminiApi(prompt, base64Image, mimeType);
    }

    private String buildCatalogContext() {
        List<Product> allProducts = productRepository.findAll();

        // Group by category and list top products
        Map<String, List<Product>> byCategory = allProducts.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory() != null ? p.getCategory().getName() : "Other"));

        StringBuilder sb = new StringBuilder();
        sb.append("EPURE PRODUCT CATALOG (use these for recommendations):\n");
        byCategory.forEach((category, products) -> {
            sb.append("\n[").append(category).append("]\n");
            products.stream().limit(5).forEach(p ->
                    sb.append("- ").append(p.getName())
                      .append(" (₹").append(p.getPrice()).append(")")
                      .append(" — ").append(p.getMaterial() != null ? p.getMaterial() : "Natural Materials")
                      .append("\n")
            );
        });
        return sb.toString();
    }

    private String buildPrompt(String catalogContext) {
        return """
                You are an expert interior designer at ÉPURE, a premium Nordic living brand. Analyze the uploaded room image and generate personalized recommendations.
                
                %s
                
                Based on the room image, provide a detailed analysis in the following EXACT JSON format (respond ONLY with valid JSON, no markdown):
                
                {
                  "roomType": "string (e.g. Bedroom, Living Room, etc.)",
                  "styleRecommendation": "string (e.g. Scandinavian Minimal, Bohemian Modern, etc.)",
                  "currentAnalysis": {
                    "existingFurniture": "string description of what is in the room",
                    "colorScheme": "string description of current colors",
                    "lightingAssessment": "string description of lighting",
                    "spaceUtilization": "string description of how space is used"
                  },
                  "furnitureSuggestions": [
                    {
                      "item": "product name from catalog or custom suggestion",
                      "fromEpure": true,
                      "reason": "why this piece fits the room",
                      "price": "price if from Epure, or estimate"
                    }
                  ],
                  "colorPalette": [
                    {
                      "name": "color name",
                      "hex": "#XXXXXX",
                      "usage": "where to apply this color"
                    }
                  ],
                  "layoutImprovementPlan": "detailed paragraph on how to rearrange and optimize the room",
                  "epureProductMatches": [
                    {
                      "productName": "exact name from catalog",
                      "reason": "why it perfectly fits this room"
                    }
                  ],
                  "customHandcraftedSuggestions": [
                    {
                      "item": "custom furniture idea",
                      "description": "handcrafted design details",
                      "estimatedPrice": "price range"
                    }
                  ],
                  "designInsight": "a single inspiring design insight or philosophy for this space"
                }
                """.formatted(catalogContext);
    }

    private String callGeminiApi(String prompt, String base64Image, String mimeType) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> imagePart = Map.of(
                "inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Image
                )
        );
        Map<String, Object> textPart = Map.of("text", prompt);

        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(imagePart, textPart)
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(content),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API error: " + e.getMessage());
        }

        return buildDemoResponse();
    }

    private String buildDemoResponse() {
        return """
                {
                  "roomType": "Living Room",
                  "styleRecommendation": "Scandinavian Minimalist with warm Nordic accents",
                  "currentAnalysis": {
                    "existingFurniture": "The room appears to have a standard sofa arrangement with a central coffee table. The furniture mix is eclectic with varying styles.",
                    "colorScheme": "Neutral beige and white tones with some darker accent pieces. The palette lacks cohesion.",
                    "lightingAssessment": "Natural light from windows is good. Artificial lighting appears warm but could be more layered and atmospheric.",
                    "spaceUtilization": "The central area is well-utilized but corner spaces are underused. There is potential to add vertical storage and greenery."
                  },
                  "furnitureSuggestions": [
                    {
                      "item": "Linen Modular Sofa",
                      "fromEpure": true,
                      "reason": "The deep-seated Belgian linen design will anchor the space with warmth and neutral tones that harmonize with the existing palette.",
                      "price": "₹35,000"
                    },
                    {
                      "item": "Modern Round Coffee Table",
                      "fromEpure": true,
                      "reason": "A low-profile round table creates visual flow and softens the angular furniture arrangement.",
                      "price": "₹5,400"
                    },
                    {
                      "item": "Scandinavian Tripod Floor Lamp",
                      "fromEpure": true,
                      "reason": "Layering light with a floor lamp in the corner adds warmth and creates a cozy reading nook atmosphere.",
                      "price": "₹8,500"
                    },
                    {
                      "item": "Forest Green Artisan Vase",
                      "fromEpure": true,
                      "reason": "A statement vase introduces organic form and a pop of nature-inspired color to the neutral setting.",
                      "price": "₹2,450"
                    },
                    {
                      "item": "Lush Monstera Deliciosa",
                      "fromEpure": true,
                      "reason": "A bold tropical plant brings life, scale and biophilic energy to the space.",
                      "price": "₹280"
                    }
                  ],
                  "colorPalette": [
                    {
                      "name": "Nordic Birch",
                      "hex": "#F5F0E8",
                      "usage": "Primary wall color for a warm, luminous base"
                    },
                    {
                      "name": "Warm Walnut",
                      "hex": "#6B4E36",
                      "usage": "Accent wall or wooden furniture tones"
                    },
                    {
                      "name": "Nordic Sage",
                      "hex": "#8BA888",
                      "usage": "Textile accents — cushions, throws, and plant pots"
                    },
                    {
                      "name": "Aged Linen",
                      "hex": "#D4C5A9",
                      "usage": "Sofa and soft furnishings"
                    },
                    {
                      "name": "Charcoal Slate",
                      "hex": "#3D3D3D",
                      "usage": "Structural details, frames, and feature objects"
                    }
                  ],
                  "layoutImprovementPlan": "Reposition the sofa to face the primary focal point of the room — either a fireplace, art wall, or large window. Float the furniture off the walls by at least 45cm to create depth and intimacy. Place the coffee table centered 50cm in front of the sofa. Introduce a rug (at least 200x300cm) to anchor the seating zone. Move lighting sources to three points — primary (overhead), secondary (floor lamp), and tertiary (table lamp) — to create a layered, dynamic ambiance. Free up corner areas for a statement plant or sculptural storage piece. Hang artwork at eye level (center at 145cm) above the sofa.",
                  "epureProductMatches": [
                    {
                      "productName": "Linen Modular Sofa",
                      "reason": "Its neutral Belgian linen upholstery and clean lines are a perfect foundation for the Nordic aesthetic of this room."
                    },
                    {
                      "productName": "Natural Woven Pillow",
                      "reason": "Adds tactile texture to the sofa arrangement and complements the earthy palette."
                    },
                    {
                      "productName": "Scandinavian Tripod Floor Lamp",
                      "reason": "Provides the warm ambient glow this room needs in its underlit corners."
                    }
                  ],
                  "customHandcraftedSuggestions": [
                    {
                      "item": "Bespoke Oak Media Console",
                      "description": "A custom-designed, handle-free media unit in white oak with integrated cable management. Crafted by Nordic artisans to fit your exact wall dimensions.",
                      "estimatedPrice": "₹22,000 – ₹35,000"
                    },
                    {
                      "item": "Hand-woven Jute Area Rug",
                      "description": "A custom 200x300cm Jute rug with a natural diamond pattern weave, hand-loomed by Indian artisans. Grounds the entire seating area with organic texture.",
                      "estimatedPrice": "₹8,000 – ₹12,000"
                    }
                  ],
                  "designInsight": "Great Nordic design is not about perfection — it is about intention. Every object should earn its place by being both beautiful and purposeful. Let light, material, and negative space tell your story."
                }
                """;
    }
}
