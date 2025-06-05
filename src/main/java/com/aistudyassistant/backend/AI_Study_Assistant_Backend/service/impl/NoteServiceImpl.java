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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

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

    // ULTIMATE debugging method to find exactly where null bytes are hiding
    private void debugNullBytes(QuizQuestionDto question, int questionNumber) {
        logger.info("🔍 DEBUGGING: Question {} null byte analysis", questionNumber);

        String[] fieldNames = {"questionText", "optionA", "optionB", "optionC", "optionD", "correctAnswer"};
        String[] fieldValues = {
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer()
        };

        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            String fieldValue = fieldValues[i];

            if (fieldValue != null) {
                // Check for different types of null bytes
                boolean hasUnicodeNull = fieldValue.contains("\u0000");
                boolean hasHexNull = fieldValue.contains("\0");
                boolean hasStringNull = fieldValue.contains("\\x00");

                // Byte-level analysis
                byte[] bytes = fieldValue.getBytes(StandardCharsets.UTF_8);
                boolean hasByteNull = false;
                int nullByteCount = 0;
                int[] nullBytePositions = new int[10]; // Track first 10 positions
                int posIndex = 0;

                for (int j = 0; j < bytes.length && posIndex < 10; j++) {
                    if (bytes[j] == 0) {
                        hasByteNull = true;
                        nullBytePositions[posIndex++] = j;
                        nullByteCount++;
                    }
                }

                // Character-level analysis
                boolean hasCharNull = false;
                for (char c : fieldValue.toCharArray()) {
                    if (c == '\0' || c == 0 || (int)c == 0) {
                        hasCharNull = true;
                        break;
                    }
                }

                logger.info("🔍 Field: {} | Length: {} | Unicode null: {} | Hex null: {} | String null: {} | Byte null: {} (count: {}) | Char null: {}",
                        fieldName,
                        fieldValue.length(),
                        hasUnicodeNull,
                        hasHexNull,
                        hasStringNull,
                        hasByteNull,
                        nullByteCount,
                        hasCharNull
                );

                if (hasByteNull && posIndex > 0) {
                    logger.error("🚨 NULL BYTE POSITIONS in {}: {}", fieldName,
                            Arrays.toString(Arrays.copyOf(nullBytePositions, posIndex)));
                }

                // Show first 100 chars with special character visualization
                String preview = fieldValue.length() > 100 ? fieldValue.substring(0, 100) + "..." : fieldValue;
                StringBuilder visualPreview = new StringBuilder();
                for (char c : preview.toCharArray()) {
                    if (c == '\0' || c == 0) {
                        visualPreview.append("[NULL]");
                    } else if (c < 32) {
                        visualPreview.append("[CTRL-").append((int)c).append("]");
                    } else {
                        visualPreview.append(c);
                    }
                }
                logger.info("🔍 Preview: {}", visualPreview.toString());
            } else {
                logger.info("🔍 Field: {} | Value: NULL", fieldName);
            }
        }
    }

    // ENHANCED cleaning method that's even more aggressive
    private String superAggressiveClean(String input) {
        if (input == null) return "";

        logger.debug("🧹 Before cleaning: length={}, preview='{}'",
                input.length(),
                input.length() > 50 ? input.substring(0, 50) + "..." : input);

        // Step 1: Convert to char array and rebuild without any problematic characters
        StringBuilder step1 = new StringBuilder();
        for (char c : input.toCharArray()) {
            // Only allow printable characters, spaces, tabs, newlines, carriage returns
            if ((c >= 32 && c <= 126) || c == ' ' || c == '\t' || c == '\n' || c == '\r' ||
                    (c >= 128 && c < 65534)) { // Allow extended ASCII and Unicode but not null-like chars
                step1.append(c);
            }
        }

        String result = step1.toString();

        // Step 2: Multiple string replacements
        result = result.replace('\u0000', ' ')           // Replace Unicode null with space
                .replace('\0', ' ')               // Replace null char with space
                .replaceAll("\\u0000", " ")       // Replace Unicode escape
                .replaceAll("\\\\x00", " ")       // Replace hex escape
                .replaceAll("\\\\0", " ")         // Replace escaped zero
                .replaceAll("\\uFFFE", " ")        // Replace BOM
                .replaceAll("\\uFFFF", " ");       // Replace invalid Unicode

        // Step 3: Byte-level cleaning
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        StringBuilder step3 = new StringBuilder();
        for (byte b : bytes) {
            if (b != 0) {  // Skip null bytes entirely
                step3.append((char) (b & 0xFF));
            } else {
                step3.append(' '); // Replace null bytes with spaces
            }
        }

        // Step 4: Final cleanup
        String finalResult = step3.toString()
                .replaceAll("\\s+", " ")  // Collapse multiple spaces
                .trim();

        logger.debug("🧹 After cleaning: length={}, preview='{}'",
                finalResult.length(),
                finalResult.length() > 50 ? finalResult.substring(0, 50) + "..." : finalResult);

        return finalResult;
    }

    // ULTIMATE text cleaning method with EXTREME null byte removal
    private String cleanTextUltimate(String input) {
        if (input == null) return "";

        // Convert to char array for byte-level cleaning
        char[] chars = input.toCharArray();
        StringBuilder cleaned = new StringBuilder(chars.length);

        for (char c : chars) {
            // Skip any character with byte value 0 or null-like values
            if (c != '\0' && c != 0 && c != '\u0000' && (int)c != 0) {
                // Also skip other problematic control characters
                if (c >= 32 || c == '\t' || c == '\n' || c == '\r') {
                    cleaned.append(c);
                }
            }
        }

        String result = cleaned.toString();

        // Additional cleaning passes
        result = result.replaceAll("\\u0000", "")
                .replaceAll("\\\\x00", "")
                .replaceAll("\\\\0", "")
                .replaceAll("\\\\u0000", "")
                .replaceAll("\\uFFFE", "")  // BOM markers
                .replaceAll("\\uFEFF", "")      // Zero width no-break space
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F-\\x9F]", "") // All control chars
                .trim();

        // Final byte-level verification
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        StringBuilder finalResult = new StringBuilder();

        for (byte b : bytes) {
            // Skip null bytes at byte level
            if (b != 0) {
                finalResult.append((char) (b & 0xFF));
            }
        }

        // Convert back to proper UTF-8 string
        try {
            String finalString = new String(finalResult.toString().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            return finalString.trim();
        } catch (Exception e) {
            // Fallback: ASCII-only safe conversion
            return result.replaceAll("[^\\x20-\\x7E\\t\\n\\r]", "").trim();
        }
    }

    // Clean the entire QuizQuestionDto object
    private QuizQuestionDto cleanQuizQuestionDto(QuizQuestionDto dto) {
        if (dto == null) return null;

        QuizQuestionDto cleanDto = new QuizQuestionDto();
        cleanDto.setQuestionText(superAggressiveClean(dto.getQuestionText()));
        cleanDto.setOptionA(superAggressiveClean(dto.getOptionA()));
        cleanDto.setOptionB(superAggressiveClean(dto.getOptionB()));
        cleanDto.setOptionC(superAggressiveClean(dto.getOptionC()));
        cleanDto.setOptionD(superAggressiveClean(dto.getOptionD()));
        cleanDto.setCorrectAnswer(superAggressiveClean(dto.getCorrectAnswer()));

        // Copy other fields that don't need text cleaning
        cleanDto.setQuizQuestionId(dto.getQuizQuestionId());
        cleanDto.setCorrectOption(dto.getCorrectOption());

        return cleanDto;
    }

    // ENHANCED Safe database save method with ultimate debugging and CORRECT OPTION FIX
    public void safeQuizQuestionSave(Long quizId, List<QuizQuestionDto> questions, String username) {
        logger.info("🛡️ SPRING BOOT: Starting ENHANCED safe database save for {} questions", questions.size());

        int savedCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestionDto question = questions.get(i);
            try {
                logger.info("🔍 DEBUGGING: Processing question {} for save", i + 1);

                // STEP 1: Debug the original question
                debugNullBytes(question, i + 1);

                // STEP 2: Apply SUPER aggressive cleaning
                QuizQuestionDto superCleanQuestion = new QuizQuestionDto();
                superCleanQuestion.setQuestionText(superAggressiveClean(question.getQuestionText()));
                superCleanQuestion.setOptionA(superAggressiveClean(question.getOptionA()));
                superCleanQuestion.setOptionB(superAggressiveClean(question.getOptionB()));
                superCleanQuestion.setOptionC(superAggressiveClean(question.getOptionC()));
                superCleanQuestion.setOptionD(superAggressiveClean(question.getOptionD()));
                superCleanQuestion.setCorrectAnswer(superAggressiveClean(question.getCorrectAnswer()));

                // Copy other fields that don't need text cleaning
                superCleanQuestion.setQuizQuestionId(question.getQuizQuestionId());

                // 🔧 CRITICAL FIX: Set the correctOption properly from correctAnswer
                if (superCleanQuestion.getCorrectAnswer() != null && !superCleanQuestion.getCorrectAnswer().isEmpty()) {
                    char correctOpt = superCleanQuestion.getCorrectAnswer().charAt(0);
                    // Ensure it's a valid option character
                    if (correctOpt == 'A' || correctOpt == 'B' || correctOpt == 'C' || correctOpt == 'D' ||
                            correctOpt == 'a' || correctOpt == 'b' || correctOpt == 'c' || correctOpt == 'd') {
                        superCleanQuestion.setCorrectOption(Character.toUpperCase(correctOpt));
                        logger.info("🔧 Set correctOption to: '{}'", Character.toUpperCase(correctOpt));
                    } else {
                        // If the correct answer is not A/B/C/D, default to 'A'
                        superCleanQuestion.setCorrectOption('A');
                        logger.warn("⚠️ Invalid correctAnswer '{}', using fallback correctOption: 'A'", correctOpt);
                    }
                } else {
                    superCleanQuestion.setCorrectOption('A'); // Default fallback
                    logger.warn("⚠️ Empty correctAnswer, using fallback correctOption: 'A'");
                }

                // Log the correctOption value for debugging
                logger.info("🔍 Final correctOption set to: '{}' (char code: {})",
                        superCleanQuestion.getCorrectOption(),
                        (int)superCleanQuestion.getCorrectOption());

                // STEP 3: Debug the cleaned question
                logger.info("🧹 After SUPER cleaning:");
                debugNullBytes(superCleanQuestion, i + 1);

                // STEP 4: Final validation with detailed logging
                boolean isValid = true;
                String[] finalFields = {
                        superCleanQuestion.getQuestionText(),
                        superCleanQuestion.getOptionA(),
                        superCleanQuestion.getOptionB(),
                        superCleanQuestion.getOptionC(),
                        superCleanQuestion.getOptionD(),
                        superCleanQuestion.getCorrectAnswer()
                };

                for (int j = 0; j < finalFields.length; j++) {
                    String field = finalFields[j];
                    if (field == null || field.trim().isEmpty()) {
                        logger.warn("⚠️ Field {} is null or empty after cleaning", j);
                        isValid = false;
                        break;
                    }

                    // Final null byte check
                    if (field.contains("\0") || field.contains("\u0000")) {
                        logger.error("🚨 CRITICAL: Field {} still contains null bytes after super cleaning!", j);
                        isValid = false;
                        break;
                    }
                }

                // Additional check for correctOption
                if (superCleanQuestion.getCorrectOption() == '\0' || superCleanQuestion.getCorrectOption() == 0) {
                    logger.error("🚨 CRITICAL: correctOption is null character! Setting to 'A'");
                    superCleanQuestion.setCorrectOption('A');
                }

                if (isValid) {
                    logger.info("✅ Question {} passed all validations, attempting database save", i + 1);
                    logger.info("🎯 Final question data: correctOption='{}', correctAnswer='{}'",
                            superCleanQuestion.getCorrectOption(),
                            superCleanQuestion.getCorrectAnswer());

                    // STEP 5: Try to save
                    List<QuizQuestionDto> singleQuestion = Arrays.asList(superCleanQuestion);
                    quizQuestionService.saveAll(quizId, singleQuestion, username);
                    savedCount++;
                    logger.info("🎉 SPRING BOOT: Successfully saved question {}/{}", i + 1, questions.size());

                } else {
                    logger.error("❌ Question {} failed validation, skipping save", i + 1);
                }

            } catch (Exception e) {
                logger.error("❌ SPRING BOOT: Failed to save question {}: {}", i + 1, e.getMessage());
                logger.error("🔍 Exception type: {}", e.getClass().getSimpleName());

                // Print the actual SQL parameters if possible
                if (e.getMessage().contains("invalid byte sequence")) {
                    logger.error("🚨 This is definitely a null byte issue in the database parameters");
                    logger.error("🔍 Check if correctOption field contains null character");
                }
            }
        }

        logger.info("🎉 SPRING BOOT: Successfully saved {}/{} questions to database", savedCount, questions.size());
    }

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
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create upload directory");
            }
        }

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
        logger.info("🚀 SPRING BOOT: Starting AI processing for note ID: {}", noteId);

        String flaskUrl = "http://localhost:5000/api/process";

        if (jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        }

        String username = jwtService.extractUsername(jwtToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("note_id", noteId);

        logger.info("SPRING BOOT: Sending request to Flask: {}", requestBody);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
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

            logger.info("SPRING BOOT: Received response from Flask API");
            logger.info("SPRING BOOT: Response keys: {}", responseBody.keySet());

            // --- Clean and save summary ---
            Object rawSummary = responseBody.get("summary");
            if (rawSummary instanceof String) {
                String originalSummary = (String) rawSummary;
                logger.info("SPRING BOOT: Processing summary with length: {}", originalSummary.length());

                String cleanedSummary = superAggressiveClean(originalSummary);

                if (!cleanedSummary.isEmpty()) {
                    SummaryDto summaryDto = new SummaryDto();
                    summaryDto.setNoteId(noteId);
                    summaryDto.setContent(cleanedSummary);
                    summaryService.save(summaryDto, username);
                    logger.info("✅ SPRING BOOT: Summary saved successfully");
                } else {
                    logger.warn("⚠️ SPRING BOOT: Summary is empty after cleaning");
                }
            } else {
                logger.warn("⚠️ SPRING BOOT: No valid summary received from Flask");
            }

            // --- Process MCQs with SUPER AGGRESSIVE cleaning ---
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mcqs = (List<Map<String, Object>>) responseBody.get("mcqs");
            logger.info("SPRING BOOT: MCQs received: {}", mcqs != null ? mcqs.size() : "null");

            if (mcqs != null && !mcqs.isEmpty()) {
                logger.info("SPRING BOOT: Processing {} MCQs", mcqs.size());

                QuizDto quizDto = new QuizDto();
                quizDto.setNoteId(noteId);
                quizDto.setCreatedAt(LocalDateTime.now());
                QuizDto savedQuiz = quizService.save(quizDto, username);
                logger.info("✅ SPRING BOOT: Quiz created with ID: {}", savedQuiz.getQuizId());

                List<QuizQuestionDto> questions = new ArrayList<>();

                for (int i = 0; i < mcqs.size(); i++) {
                    Map<String, Object> mcq = mcqs.get(i);
                    try {
                        logger.info("SPRING BOOT: Processing MCQ {}/{}", i + 1, mcqs.size());

                        QuizQuestionDto q = new QuizQuestionDto();

                        // STEP 1: Clean the raw data from Flask with SUPER AGGRESSIVE cleaning
                        String rawQuestion = superAggressiveClean(String.valueOf(mcq.get("question")));
                        String rawCorrectAnswer = superAggressiveClean(String.valueOf(mcq.get("correct_answer")));

                        if (rawQuestion.isEmpty() || rawQuestion.equals("null")) {
                            logger.warn("SPRING BOOT: Skipping MCQ {} - empty question after cleaning", i + 1);
                            continue;
                        }

                        if (rawCorrectAnswer.isEmpty() || rawCorrectAnswer.equals("null")) {
                            logger.warn("SPRING BOOT: Skipping MCQ {} - empty correct answer after cleaning", i + 1);
                            continue;
                        }

                        q.setQuestionText(rawQuestion);
                        q.setCorrectAnswer(rawCorrectAnswer);

                        logger.info("SPRING BOOT: Question {}: '{}'", i + 1,
                                rawQuestion.length() > 50 ? rawQuestion.substring(0, 50) + "..." : rawQuestion);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> options = (Map<String, Object>) mcq.get("options");
                        if (options == null || options.isEmpty()) {
                            logger.warn("SPRING BOOT: Skipping MCQ {} - no options provided", i + 1);
                            continue;
                        }

                        // STEP 2: Clean all options with SUPER AGGRESSIVE cleaning
                        String optionA = superAggressiveClean(String.valueOf(options.get("A")));
                        String optionB = superAggressiveClean(String.valueOf(options.get("B")));
                        String optionC = superAggressiveClean(String.valueOf(options.get("C")));
                        String optionD = superAggressiveClean(String.valueOf(options.get("D")));

                        // Validate that options exist after cleaning
                        if (optionA.isEmpty() || optionA.equals("null") ||
                                optionB.isEmpty() || optionB.equals("null") ||
                                optionC.isEmpty() || optionC.equals("null") ||
                                optionD.isEmpty() || optionD.equals("null")) {
                            logger.warn("SPRING BOOT: Skipping MCQ {} - empty options after super aggressive cleaning", i + 1);
                            continue;
                        }

                        q.setOptionA(optionA);
                        q.setOptionB(optionB);
                        q.setOptionC(optionC);
                        q.setOptionD(optionD);

                        // STEP 3: Apply final DTO-level cleaning
                        QuizQuestionDto cleanedQ = cleanQuizQuestionDto(q);

                        // STEP 4: Final validation
                        if (cleanedQ != null &&
                                cleanedQ.getQuestionText() != null && !cleanedQ.getQuestionText().isEmpty() &&
                                cleanedQ.getCorrectAnswer() != null && !cleanedQ.getCorrectAnswer().isEmpty() &&
                                cleanedQ.getOptionA() != null && !cleanedQ.getOptionA().isEmpty() &&
                                cleanedQ.getOptionB() != null && !cleanedQ.getOptionB().isEmpty() &&
                                cleanedQ.getOptionC() != null && !cleanedQ.getOptionC().isEmpty() &&
                                cleanedQ.getOptionD() != null && !cleanedQ.getOptionD().isEmpty()) {

                            questions.add(cleanedQ);
                            logger.info("✅ SPRING BOOT: MCQ {} processed and super-cleaned successfully", i + 1);
                        } else {
                            logger.warn("⚠️ SPRING BOOT: MCQ {} failed final DTO validation", i + 1);
                        }

                    } catch (Exception e) {
                        logger.error("❌ SPRING BOOT: Error processing MCQ {}: {}", i + 1, e.getMessage(), e);
                    }
                }

                logger.info("SPRING BOOT: Processed {} validated questions for database save", questions.size());

                if (!questions.isEmpty()) {
                    // Use our enhanced safe save method
                    safeQuizQuestionSave(savedQuiz.getQuizId(), questions, username);
                } else {
                    logger.warn("⚠️ SPRING BOOT: No questions were valid for saving");
                }
            } else {
                logger.info("SPRING BOOT: No MCQs received from Flask API");
            }

            logger.info("🎉 SPRING BOOT: AI processing completed for note ID: {}", noteId);

        } catch (Exception e) {
            logger.error("❌ SPRING BOOT: Error calling Flask API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process note with AI: " + e.getMessage());
        }
    }
}