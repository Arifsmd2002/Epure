package com.project2.controller;

import com.project2.service.RoomAnalyzerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/room-analyzer")
public class RoomAnalyzerController {

    @Autowired
    private RoomAnalyzerService roomAnalyzerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeRoom(@RequestParam("image") MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Please upload a room image to analyze."
            ));
        }

        // Validate file type
        String contentType = imageFile.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Please upload a valid image file (JPG, PNG, WEBP)."
            ));
        }

        // Validate file size (max 10MB)
        if (imageFile.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Image file must be less than 10MB."
            ));
        }

        try {
            String jsonResult = roomAnalyzerService.analyzeRoom(imageFile);

            // Parse JSON to verify it's valid, then return as parsed object
            Object parsedResult = objectMapper.readValue(jsonResult, Object.class);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "analysis", parsedResult
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Analysis failed: " + e.getMessage()
            ));
        }
    }
}
