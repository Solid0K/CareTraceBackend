package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.GameAlreadyCompletedException;
import com.krishu.caretracev2.CustomExceptions.GameNotCompleteException;
import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.FlashCardAnswerRequest;
import com.krishu.caretracev2.DTO.FlashCardAnswerResponse;
import com.krishu.caretracev2.DTO.FlashCardGameSummaryResponse;
import com.krishu.caretracev2.DTO.FlashCardQuestionResponse;
import com.krishu.caretracev2.Model.FlashCardGameSession;
import com.krishu.caretracev2.Model.ImportantPerson;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.FlashCardGameSessionRepo;
import com.krishu.caretracev2.Repository.ImportantPersonRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // shared stubs in setUp() are not used by every test
class FlashCardGameServiceTest {

    private static final String SIGNED_PREFIX = "https://signed.example/";

    @Mock
    private FlashCardGameSessionRepo flashCardGameSessionRepo;
    @Mock
    private ImportantPersonService importantPersonService;
    @Mock
    private ImportantPersonRepo importantPersonRepo;
    @Mock
    private PatientRepo patientRepo;
    @Mock
    private PhotoStorageService photoStorageService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private FlashCardGameService flashCardGameService;

    private Patient patient;
    private ImportantPerson wife;
    private FlashCardGameSession session;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient-1");

        wife = person("person-1", "wife", "patient-1");

        // a one-question game, already on question 1
        session = new FlashCardGameSession();
        session.setId("session-1");
        session.setPatientId("patient-1");
        session.setPersonIds(List.of("person-1"));
        session.setQuestinIndex(0);
        session.setScore(0);
        session.setCompleted(false);

        when(authentication.getName()).thenReturn("user-1");
        when(patientRepo.findByUserId("user-1")).thenReturn(Optional.of(patient));
        when(flashCardGameSessionRepo.findById("session-1")).thenReturn(Optional.of(session));

        // saving returns the same object, and gives new sessions an id
        when(flashCardGameSessionRepo.save(any(FlashCardGameSession.class))).thenAnswer(invocation -> {
            FlashCardGameSession saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId("session-new");
            }
            return saved;
        });
    }

    // ------------------------------------------------------------------ helpers

    private ImportantPerson person(String id, String relation, String patientId) {
        ImportantPerson p = new ImportantPerson();
        p.setId(id);
        p.setRelation(relation);
        p.setPatientId(patientId);
        p.setPhotoKey("patients/" + patientId + "/people/" + id);
        return p;
    }

    /** Makes the repos and the photo service answer for these people. */
    private void stubPeople(ImportantPerson... people) {
        Map<String, ImportantPerson> byId = new HashMap<>();
        for (ImportantPerson p : people) {
            byId.put(p.getId(), p);
        }
        when(importantPersonRepo.findById(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(byId.get(invocation.getArgument(0, String.class))));
        when(importantPersonRepo.findByPatientId("patient-1")).thenReturn(List.of(people));
        when(photoStorageService.photoUrl(anyString())).thenAnswer(invocation ->
                SIGNED_PREFIX + invocation.getArgument(0, String.class));
    }

    private FlashCardAnswerRequest answer(String personId, String text) {
        FlashCardAnswerRequest request = new FlashCardAnswerRequest();
        request.setPersonId(personId);
        request.setAnswer(text);
        return request;
    }

    // ---------------------------------------------------------------- startGame

    @Test
    void startGame_returnsFirstQuestionWithSignedPhotoUrlAndFourOptions() {
        ImportantPerson son = person("person-2", "son", "patient-1");
        ImportantPerson daughter = person("person-3", "daughter", "patient-1");
        ImportantPerson brother = person("person-4", "brother", "patient-1");
        stubPeople(wife, son, daughter, brother);
        // must be a modifiable list: the service shuffles it
        when(importantPersonService.getPersonsPhotos("patient-1"))
                .thenReturn(new ArrayList<>(List.of(wife, son, daughter, brother)));

        FlashCardQuestionResponse response = flashCardGameService.startGame(authentication);

        assertEquals("session-new", response.getSessionId());
        assertEquals(1, response.getQuestionNumber());
        assertEquals(4, response.getTotalQuestions());

        // the photo is a signed link, never the raw S3 key
        ImportantPerson asked = importantPersonRepo.findById(response.getPersonId()).orElseThrow();
        assertEquals(SIGNED_PREFIX + asked.getPhotoKey(), response.getPhotoUrl());

        // four different choices, one of them correct
        assertEquals(4, response.getOptions().size());
        assertEquals(4, new HashSet<>(response.getOptions()).size());
        assertTrue(response.getOptions().contains(asked.getRelation()));

        ArgumentCaptor<FlashCardGameSession> captor = ArgumentCaptor.forClass(FlashCardGameSession.class);
        verify(flashCardGameSessionRepo).save(captor.capture());
        FlashCardGameSession saved = captor.getValue();
        assertEquals("patient-1", saved.getPatientId());
        assertEquals(4, saved.getPersonIds().size());
        assertEquals(0, saved.getQuestinIndex());
        assertEquals(0, saved.getScore());
        assertFalse(saved.getCompleted());
        assertEquals(response.getOptions(), saved.getCurrOptions());
    }

    @Test
    void startGame_noPeopleWithPhotos_throwsNotFoundAndSavesNothing() {
        when(importantPersonService.getPersonsPhotos("patient-1")).thenReturn(new ArrayList<>());

        assertThrows(NotFoundException.class, () -> flashCardGameService.startGame(authentication));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    @Test
    void startGame_patientNotFound_throwsNotFound() {
        when(patientRepo.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> flashCardGameService.startGame(authentication));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    // -------------------------------------------------------------- submitAnswer

    @Test
    void submitAnswer_correctAnswer_increasesScoreAndCompletesGame() {
        stubPeople(wife);

        FlashCardAnswerResponse response =
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1");

        assertTrue(response.getCorrect());
        assertEquals(1, response.getScore());
        assertTrue(response.getCompleted());
        assertNull(response.getNextQuestion());

        assertEquals(1, session.getScore());
        assertEquals(1, session.getQuestinIndex());
        assertTrue(session.getCompleted());
        verify(flashCardGameSessionRepo).save(session);
    }

    @Test
    void submitAnswer_answerIgnoresCaseAndSurroundingSpaces() {
        stubPeople(wife);

        FlashCardAnswerResponse response =
                flashCardGameService.submitAnswer(answer("person-1", "  WIFE "), authentication, "session-1");

        assertTrue(response.getCorrect());
        assertEquals(1, response.getScore());
    }

    @Test
    void submitAnswer_incorrectAnswer_doesNotIncreaseScore() {
        stubPeople(wife);

        FlashCardAnswerResponse response =
                flashCardGameService.submitAnswer(answer("person-1", "dad"), authentication, "session-1");

        assertFalse(response.getCorrect());
        assertEquals(0, response.getScore());
        assertTrue(response.getCompleted());
        assertNull(response.getNextQuestion());

        assertEquals(0, session.getScore());
        assertEquals(1, session.getQuestinIndex());
        assertTrue(session.getCompleted());
        verify(flashCardGameSessionRepo).save(session);
    }

    @Test
    void submitAnswer_notLastQuestion_returnsNextQuestionWithSignedPhotoUrl() {
        ImportantPerson son = person("person-2", "son", "patient-1");
        stubPeople(wife, son);
        session.setPersonIds(List.of("person-1", "person-2"));

        FlashCardAnswerResponse response =
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1");

        assertTrue(response.getCorrect());
        assertEquals(1, response.getScore());
        assertFalse(response.getCompleted());

        FlashCardQuestionResponse next = response.getNextQuestion();
        assertNotNull(next);
        assertEquals("person-2", next.getPersonId());
        assertEquals(2, next.getQuestionNumber());
        assertEquals(SIGNED_PREFIX + son.getPhotoKey(), next.getPhotoUrl());
        assertTrue(next.getOptions().contains("son"));

        assertEquals(1, session.getQuestinIndex());
        assertFalse(session.getCompleted());
        verify(flashCardGameSessionRepo).save(session);
    }

    @Test
    void submitAnswer_notTheCurrentQuestion_throwsAndSavesNothing() {
        stubPeople(wife);

        assertThrows(IllegalArgumentException.class, () ->
                flashCardGameService.submitAnswer(answer("person-99", "wife"), authentication, "session-1"));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    @Test
    void submitAnswer_gameAlreadyCompleted_throwsGameAlreadyCompleted() {
        session.setCompleted(true);

        assertThrows(GameAlreadyCompletedException.class, () ->
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1"));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    @Test
    void submitAnswer_sessionNotFound_throwsNotFound() {
        when(flashCardGameSessionRepo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "missing"));
    }

    @Test
    void submitAnswer_otherPatientsSession_throwsUnauthorized() {
        session.setPatientId("another-patient");

        assertThrows(UnauthorizedException.class, () ->
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1"));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    @Test
    void submitAnswer_personBelongsToAnotherPatient_throwsUnauthorized() {
        wife.setPatientId("another-patient");
        stubPeople(wife);

        assertThrows(UnauthorizedException.class, () ->
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1"));

        verify(flashCardGameSessionRepo, never()).save(any(FlashCardGameSession.class));
    }

    @Test
    void submitAnswer_concurrentUpdate_throwsConflict() {
        stubPeople(wife);
        when(flashCardGameSessionRepo.save(any(FlashCardGameSession.class)))
                .thenThrow(new OptimisticLockingFailureException("Concurrent update"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                flashCardGameService.submitAnswer(answer("person-1", "wife"), authentication, "session-1"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    // ------------------------------------------------------------- getGameSummary

    @Test
    void getGameSummary_completedGame_returnsScoreAndTotal() {
        session.setCompleted(true);
        session.setScore(1);

        FlashCardGameSummaryResponse summary = flashCardGameService.getGameSummary("session-1", authentication);

        assertEquals("session-1", summary.getSessionId());
        assertEquals(1, summary.getScore().intValue());
        assertEquals(1, summary.getTotalQuestion().intValue());
        assertTrue(summary.getCompleted());
    }

    @Test
    void getGameSummary_gameNotFinished_throwsGameNotComplete() {
        assertThrows(GameNotCompleteException.class, () ->
                flashCardGameService.getGameSummary("session-1", authentication));
    }

    @Test
    void getGameSummary_otherPatientsSession_throwsUnauthorized() {
        session.setCompleted(true);
        session.setPatientId("another-patient");

        assertThrows(UnauthorizedException.class, () ->
                flashCardGameService.getGameSummary("session-1", authentication));
    }
}