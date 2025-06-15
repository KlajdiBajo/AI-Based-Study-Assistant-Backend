package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(UserDashboardServiceImpl.class);

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Override
    public UserDashboardDto getDashboardOverview(String username) {
        User user = findUserByEmail(username);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Get all attempts for comprehensive analysis
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByUser(user);
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserAndAttemptedAtAfter(user, sevenDaysAgo);

        // Calculate basic statistics
        int totalQuizzes = allAttempts.size();
        double averageScore = allAttempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);

        int bestScore = allAttempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .max()
                .orElse(0);

        int quizzesThisWeek = recentAttempts.size();
        double averageScoreThisWeek = recentAttempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);

        // Get study streak
        StudyStreakDto streakInfo = getStudyStreak(username);

        // Calculate total study time (approximate based on quiz attempts)
        int totalStudyTime = calculateEstimatedStudyTime(allAttempts);

        // Get last study session
        LocalDateTime lastStudySession = allAttempts.stream()
                .map(QuizAttempt::getAttemptedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Get performance insights
        List<NotePerformanceDto> notePerformances = getNotePerformance(username);
        List<NotePerformanceDto> topPerforming = notePerformances.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<NotePerformanceDto> weakest = notePerformances.stream()
                .sorted(Comparator.comparing(NotePerformanceDto::getAverageScore))
                .limit(3)
                .collect(Collectors.toList());

        // Get performance trend
        PerformanceTrendDto performanceTrend = getScoreTrends(username, 30);

        // Get recent activities
        List<RecentActivityDto> recentActivities = getRecentActivities(username, 5);

        // Get recommendations
        List<StudyRecommendationDto> recommendations = getPersonalizedRecommendations(username);

        logger.info("Generated dashboard overview for user: {}", username);

        return UserDashboardDto.builder()
                .totalQuizzesTaken(totalQuizzes)
                .averageScore(averageScore)
                .bestScore(bestScore)
                .quizzesThisWeek(quizzesThisWeek)
                .averageScoreThisWeek(averageScoreThisWeek)
                .currentStreak(streakInfo.getCurrentStreak())
                .totalStudyTimeMinutes(totalStudyTime)
                .lastStudySession(lastStudySession)
                .topPerformingNotes(topPerforming)
                .weakestNotes(weakest)
                .performanceTrend(performanceTrend)
                .recentActivities(recentActivities)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public PerformanceTrendDto getScoreTrends(String username, int days) {
        User user = findUserByEmail(username);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserAndAttemptedAtAfterOrderByAttemptedAt(user, startDate);

        // Group by date and calculate daily averages
        Map<LocalDate, List<QuizAttempt>> attemptsByDate = attempts.stream()
                .collect(Collectors.groupingBy(attempt -> attempt.getAttemptedAt().toLocalDate()));

        List<DailyScoreDto> dailyScores = attemptsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<QuizAttempt> dayAttempts = entry.getValue();

                    double avgScore = dayAttempts.stream()
                            .mapToInt(QuizAttempt::getScore)
                            .average()
                            .orElse(0.0);

                    int totalQuestions = dayAttempts.stream()
                            .mapToInt(attempt -> attempt.getAnswers() != null ? attempt.getAnswers().size() : 0)
                            .sum();

                    return DailyScoreDto.builder()
                            .date(date)
                            .averageScore(avgScore)
                            .attemptCount(dayAttempts.size())
                            .totalQuestions(totalQuestions)
                            .build();
                })
                .sorted(Comparator.comparing(DailyScoreDto::getDate))
                .collect(Collectors.toList());

        // Calculate trend
        String trendDirection = calculateTrendDirection(dailyScores);
        double trendPercentage = calculateTrendPercentage(dailyScores);
        String summary = generateTrendSummary(trendDirection, trendPercentage);

        return PerformanceTrendDto.builder()
                .dailyScores(dailyScores)
                .trendDirection(trendDirection)
                .trendPercentage(trendPercentage)
                .summary(summary)
                .build();
    }

    @Override
    public List<NotePerformanceDto> getNotePerformance(String username) {
        User user = findUserByEmail(username);
        List<QuizAttempt> attempts = quizAttemptRepository.findByUser(user);

        // Group by individual notes
        Map<Note, List<QuizAttempt>> attemptsByNote = attempts.stream()
                .collect(Collectors.groupingBy(attempt -> attempt.getQuiz().getNote()));

        return attemptsByNote.entrySet().stream()
                .map(entry -> {
                    Note note = entry.getKey();
                    List<QuizAttempt> noteAttempts = entry.getValue();

                    double avgScore = noteAttempts.stream()
                            .mapToInt(QuizAttempt::getScore)
                            .average()
                            .orElse(0.0);

                    int bestScore = noteAttempts.stream()
                            .mapToInt(QuizAttempt::getScore)
                            .max()
                            .orElse(0);

                    LocalDateTime lastAttempt = noteAttempts.stream()
                            .map(QuizAttempt::getAttemptedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    int totalQuestions = noteAttempts.stream()
                            .mapToInt(attempt -> attempt.getAnswers() != null ? attempt.getAnswers().size() : 0)
                            .sum();

                    int correctAnswers = noteAttempts.stream()
                            .mapToInt(QuizAttempt::getScore)
                            .sum();

                    double improvementRate = calculateImprovementRate(noteAttempts);
                    String performanceLevel = determinePerformanceLevel(avgScore, totalQuestions > 0 ? totalQuestions / noteAttempts.size() : 0);

                    return NotePerformanceDto.builder()
                            .noteId(note.getId())
                            .noteTitle(note.getFileName())
                            .fileName(note.getFileName())
                            .totalAttempts(noteAttempts.size())
                            .averageScore(avgScore)
                            .bestScore(bestScore)
                            .lastAttemptDate(lastAttempt)
                            .improvementRate(improvementRate)
                            .totalQuestions(totalQuestions)
                            .correctAnswers(correctAnswers)
                            .performanceLevel(performanceLevel)
                            .build();
                })
                .sorted(Comparator.comparing(NotePerformanceDto::getAverageScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<WeakAreaDto> getWeakAreas(String username) {
        User user = findUserByEmail(username);

        List<QuizAnswer> allAnswers = quizAnswerRepository.findByAttemptUser(user);

        // Group by NOTE instead of subject
        Map<Note, List<QuizAnswer>> answersByNote = allAnswers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getQuestion().getQuiz().getNote()));

        return answersByNote.entrySet().stream()
                .map(entry -> {
                    Note note = entry.getKey();
                    List<QuizAnswer> noteAnswers = entry.getValue();

                    long mistakeCount = noteAnswers.stream()
                            .filter(answer -> !answer.isCorrect())
                            .count();

                    double errorRate = noteAnswers.size() > 0 ?
                            (double) mistakeCount / noteAnswers.size() * 100 : 0;

                    int questionsAttempted = noteAnswers.size();
                    int correctAnswers = (int) noteAnswers.stream()
                            .filter(QuizAnswer::isCorrect)
                            .count();

                    return WeakAreaDto.builder()
                            .noteTitle(note.getFileName())
                            .fileName(note.getFileName())
                            .mistakeCount((int) mistakeCount)
                            .errorRate(errorRate)
                            .questionsAttempted(questionsAttempted)
                            .correctAnswers(correctAnswers)
                            .recommendedAction("Review questions from: " + note.getTitle())
                            .build();
                })
                .filter(area -> area.getErrorRate() > 20)
                .sorted(Comparator.comparing(WeakAreaDto::getErrorRate).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public StudyStreakDto getStudyStreak(String username) {
        User user = findUserByEmail(username);

        // Get all quiz attempt dates
        List<LocalDate> studyDates = quizAttemptRepository.findByUser(user).stream()
                .map(attempt -> attempt.getAttemptedAt().toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (studyDates.isEmpty()) {
            return StudyStreakDto.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .isActiveToday(false)
                    .studyDates(new ArrayList<>())
                    .build();
        }

        // Calculate current streak
        int currentStreak = calculateCurrentStreak(studyDates);
        int longestStreak = calculateLongestStreak(studyDates);

        LocalDate streakStartDate = studyDates.size() >= currentStreak ?
                studyDates.get(currentStreak - 1) : studyDates.get(studyDates.size() - 1);

        LocalDate lastStudyDate = studyDates.get(0);
        boolean isActiveToday = lastStudyDate.equals(LocalDate.now());

        return StudyStreakDto.builder()
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .streakStartDate(streakStartDate)
                .lastStudyDate(lastStudyDate)
                .studyDates(studyDates)
                .isActiveToday(isActiveToday)
                .build();
    }

    @Override
    public List<StudyRecommendationDto> getPersonalizedRecommendations(String username) {
        List<StudyRecommendationDto> recommendations = new ArrayList<>();

        // Get weak areas and suggest practice
        List<WeakAreaDto> weakAreas = getWeakAreas(username);
        for (WeakAreaDto weakArea : weakAreas.stream().limit(2).collect(Collectors.toList())) {
            recommendations.add(StudyRecommendationDto.builder()
                    .type("WEAK_NOTE")
                    .title("Practice " + weakArea.getNoteTitle())
                    .description("You have a " + String.format("%.1f", weakArea.getErrorRate()) + "% error rate in this note")
                    .noteTitle(weakArea.getNoteTitle())
                    .actionText("Retake Quiz")
                    .actionUrl("/quizzes?note=" + weakArea.getNoteTitle())
                    .priority(5)
                    .build());
        }

        // Check study streak and encourage daily practice
        StudyStreakDto streak = getStudyStreak(username);
        if (!streak.isActiveToday() && streak.getCurrentStreak() > 0) {
            recommendations.add(StudyRecommendationDto.builder()
                    .type("MAINTAIN_STREAK")
                    .title("Maintain Your Study Streak!")
                    .description("You have a " + streak.getCurrentStreak() + "-day streak. Don't break it now!")
                    .actionText("Start Studying")
                    .actionUrl("/quizzes")
                    .priority(4)
                    .build());
        }

        // Suggest new notes if user is doing well
        List<NotePerformanceDto> notes = getNotePerformance(username);
        if (notes.stream().anyMatch(s -> s.getAverageScore() > 8)) {
            recommendations.add(StudyRecommendationDto.builder()
                    .type("NEW_TOPIC")
                    .title("Try a New Topic")
                    .description("You're excelling in your current notes. Ready for a new challenge?")
                    .actionText("Upload New Note")
                    .actionUrl("/notes")
                    .priority(2)
                    .build());
        }

        return recommendations.stream()
                .sorted(Comparator.comparing(StudyRecommendationDto::getPriority).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentActivityDto> getRecentActivities(String username, int limit) {
        User user = findUserByEmail(username);

        List<RecentActivityDto> activities = new ArrayList<>();

        // Add recent quiz attempts
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserOrderByAttemptedAtDesc(user).stream()
                .limit(limit)
                .collect(Collectors.toList());

        for (QuizAttempt attempt : recentAttempts) {
            String description = String.format("Completed quiz \"%s\" with score %d/%d",
                    attempt.getQuiz().getNote().getFileName(),
                    attempt.getScore(),
                    attempt.getAnswers() != null ? attempt.getAnswers().size() : 0);

            activities.add(RecentActivityDto.builder()
                    .activityType("QUIZ_COMPLETED")
                    .description(description)
                    .timestamp(attempt.getAttemptedAt())
                    .noteTitle(attempt.getQuiz().getNote().getFileName())
                    .score(attempt.getScore())
                    .relatedId(attempt.getQuiz().getQuizId())
                    .build());
        }

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDto::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Helper methods
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private int calculateEstimatedStudyTime(List<QuizAttempt> attempts) {
        // Estimate 2 minutes per question on average
        return attempts.stream()
                .mapToInt(attempt -> attempt.getAnswers() != null ? attempt.getAnswers().size() * 2 : 10)
                .sum();
    }

    private double calculateImprovementRate(List<QuizAttempt> attempts) {
        if (attempts.size() < 2) return 0.0;

        List<QuizAttempt> sortedAttempts = attempts.stream()
                .sorted(Comparator.comparing(QuizAttempt::getAttemptedAt))
                .collect(Collectors.toList());

        QuizAttempt first = sortedAttempts.get(0);
        QuizAttempt last = sortedAttempts.get(sortedAttempts.size() - 1);

        if (first.getScore() == 0) return 0.0;

        return ((double) (last.getScore() - first.getScore()) / first.getScore()) * 100;
    }

    private String determinePerformanceLevel(double averageScore, int averageQuestionsPerQuiz) {
        if (averageQuestionsPerQuiz == 0) return "No Data";

        double percentage = (averageScore / averageQuestionsPerQuiz) * 100;

        if (percentage >= 80) return "Excellent";
        else if (percentage >= 60) return "Good";
        else return "Needs Improvement";
    }

    private String calculateTrendDirection(List<DailyScoreDto> dailyScores) {
        if (dailyScores.size() < 2) return "STABLE";

        double firstHalf = dailyScores.stream()
                .limit(dailyScores.size() / 2)
                .mapToDouble(DailyScoreDto::getAverageScore)
                .average()
                .orElse(0.0);

        double secondHalf = dailyScores.stream()
                .skip(dailyScores.size() / 2)
                .mapToDouble(DailyScoreDto::getAverageScore)
                .average()
                .orElse(0.0);

        if (secondHalf > firstHalf * 1.1) return "IMPROVING";
        else if (secondHalf < firstHalf * 0.9) return "DECLINING";
        else return "STABLE";
    }

    private double calculateTrendPercentage(List<DailyScoreDto> dailyScores) {
        if (dailyScores.size() < 2) return 0.0;

        double firstHalf = dailyScores.stream()
                .limit(dailyScores.size() / 2)
                .mapToDouble(DailyScoreDto::getAverageScore)
                .average()
                .orElse(0.0);

        double secondHalf = dailyScores.stream()
                .skip(dailyScores.size() / 2)
                .mapToDouble(DailyScoreDto::getAverageScore)
                .average()
                .orElse(0.0);

        if (firstHalf == 0) return 0.0;

        return ((secondHalf - firstHalf) / firstHalf) * 100;
    }

    private String generateTrendSummary(String direction, double percentage) {
        switch (direction) {
            case "IMPROVING":
                return String.format("Your performance is improving by %.1f%%", Math.abs(percentage));
            case "DECLINING":
                return String.format("Your performance has declined by %.1f%%", Math.abs(percentage));
            default:
                return "Your performance is stable";
        }
    }

    private int calculateCurrentStreak(List<LocalDate> studyDates) {
        if (studyDates.isEmpty()) return 0;

        int streak = 1;
        LocalDate current = studyDates.get(0);

        for (int i = 1; i < studyDates.size(); i++) {
            LocalDate previous = studyDates.get(i);
            if (current.minusDays(1).equals(previous)) {
                streak++;
                current = previous;
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> studyDates) {
        if (studyDates.isEmpty()) return 0;

        int longestStreak = 1;
        int currentStreak = 1;

        // Sort dates in ascending order for proper streak calculation
        List<LocalDate> sortedDates = studyDates.stream()
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate current = sortedDates.get(i);
            LocalDate previous = sortedDates.get(i - 1);

            if (current.equals(previous.plusDays(1))) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }
}