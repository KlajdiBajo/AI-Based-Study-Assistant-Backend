package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.*;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final Mapper<Note, NoteDto> noteMapper;
    private final SummaryService summaryService;
    private final QuizService quizService;
    private final QuizQuestionService quizQuestionService;
    private final RestTemplate restTemplate;
    private final Mapper<Summary, SummaryDto> summaryMapper;
    private final Mapper<Quiz, QuizDto> quizMapper;
    private final Mapper<QuizQuestion, QuizQuestionDto> quizQuestionMapper;
    private final JwtService jwtService;


    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public NoteDto saveNote(MultipartFile file, NoteDto dto) throws IOException {
        // Save file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueName = UUID.randomUUID() + extension;

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(dir, uniqueName);
        file.transferTo(dest);

        // Update DTO
        dto.setFileName(originalFilename);
        dto.setFileURL(dest.getAbsolutePath());
        dto.setStatus("uploaded");
        dto.setUploadedAt(LocalDateTime.now());

        Note note = noteMapper.mapFrom(dto);
        Note saved = noteRepository.save(note);
        return noteMapper.mapTo(saved);
    }

    @Override
    public List<NoteDto> getNotesByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return noteRepository.findByUser(user).stream()
                .map(noteMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NoteDto> getNoteById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return noteRepository.findById(id)
                .filter(note -> note.getUser().getId().equals(user.getId()))
                .map(noteMapper::mapTo);
    }

    @Override
    public void deleteNoteById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        noteRepository.findById(id)
                .filter(note -> note.getUser().getId().equals(user.getId()))
                .ifPresentOrElse(
                        noteRepository::delete,
                        () -> { throw new RuntimeException("Unauthorized or note not found"); }
                );
    }

    @Override
    public void processNoteWithAiModel(Long noteId, String jwtToken) {
        String flaskUrl = "http://localhost:5000/api/process";

        // Clean JWT token
        if (jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        }

        // Extract username from token (assuming you have a JwtService — or parse manually)
        String username = jwtService.extractUsername(jwtToken);

        // Prepare headers and body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken); // Flask expects Bearer prefix

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("note_id", noteId); // Flask expects "note_id"

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                flaskUrl,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Empty response from the AI Service!");
        }

        //Save summary
        String summaryText = (String) responseBody.get("summary");
        SummaryDto summaryDto = new SummaryDto();
        summaryDto.setNoteId(noteId);
        summaryDto.setContent(summaryText);
        summaryService.save(summaryDto, username);

        //Save quiz and questions
        List<Map<String, Object>> mcqs = (List<Map<String, Object>>) responseBody.get("mcqs");
        if (mcqs != null && !mcqs.isEmpty()) {
            QuizDto quizDto = new QuizDto();
            quizDto.setNoteId(noteId);
            QuizDto savedQuiz = quizService.save(quizDto, username);

            List<QuizQuestionDto> questions = mcqs.stream().map(mcq -> {
                QuizQuestionDto q = new QuizQuestionDto();
                q.setQuizId(savedQuiz.getQuizId());
                q.setQuestion((String) mcq.get("question"));
                q.setCorrectAnswer((String) mcq.get("correct_answer"));

                Map<String, String> options = (Map<String, String>) mcq.get("options");
                q.setOptionA(options.get("A"));
                q.setOptionB(options.get("B"));
                q.setOptionC(options.get("C"));
                q.setOptionD(options.get("D"));
                return q;
            }).collect(Collectors.toList());

            quizQuestionService.saveAll(savedQuiz.getQuizId(), questions, username);
        }
    }

}
