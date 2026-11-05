package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.GameAlreadyCompletedException;
import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.OutOfRangeException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.PlaceGuesserAnswerRequest;
import com.krishu.caretracev2.DTO.PlaceGuesserAnswerResponse;
import com.krishu.caretracev2.DTO.PlaceGuesserQuestionResponse;
import com.krishu.caretracev2.DTO.PlaceGuesserSummaryResponse;
import com.krishu.caretracev2.Model.ImportantPlace;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Model.PlaceGuesserSession;
import com.krishu.caretracev2.Repository.ImportantPlaceRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import com.krishu.caretracev2.Repository.PlaceGuesserRepo;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class PlaceGuesserService {

    private final PlaceGuesserRepo placeGuesserRepo;
    private final ImportantPlaceRepo placeRepo;
    private final PatientRepo patientRepo;
    private final PhotoStorageService photoStorageService;

    private final Integer MAX_QUESTION=5;
    private final Integer MAX_OPTION=4;

    public PlaceGuesserService(PlaceGuesserRepo placeGuesserRepo,ImportantPlaceRepo placeRepo
            ,PatientRepo patientRepo,PhotoStorageService photoStorageService){
        this.placeGuesserRepo=placeGuesserRepo;
        this.placeRepo=placeRepo;
        this.patientRepo=patientRepo;
        this.photoStorageService=photoStorageService;
    }

    public PlaceGuesserQuestionResponse startGame(Authentication authentication){
        Patient patient=patientRepo.findByUserId(authentication.getName())
                .orElseThrow(()->new NotFoundException("Patient not found"));
        List<ImportantPlace> places=placeRepo.findByPatientId(patient.getId()).stream()
                .filter(place->place.getPhotoKey()!=null)
                .toList();
        List<ImportantPlace> uniquePlace=new ArrayList<>(new LinkedHashSet<>(places.stream().
                map(ImportantPlace::getName).toList().stream().
                map(name->places.stream().
                        filter(p->p.getName().
                                equals(name)).findFirst().orElseThrow()).toList()));

        if(uniquePlace.size()<2){
            throw new IllegalArgumentException("At least two places with photos and " +
                    "different names are required");
        }
        Collections.shuffle(uniquePlace);
        int questionCount=Math.min(MAX_QUESTION,uniquePlace.size());
        List<String> placeIds=uniquePlace.stream().limit(questionCount)
                .map(ImportantPlace::getId).toList();
        PlaceGuesserSession session=new PlaceGuesserSession();
        session.setPatientId(patient.getId());
        session.setImportantPlaceIds(placeIds);
        session.setCurrQuestionIndex(0);
        session.setScore(0);
        session.setTotalQuestions(questionCount);
        session.setCompleted(false);
        session.setStartedAt(LocalDateTime.now());

        PlaceGuesserQuestionResponse response=buildCurrentQuestion(session);
        PlaceGuesserSession savedSession=placeGuesserRepo.save(session);
        response.setSessionId(savedSession.getId());
        return response;
    }

    public PlaceGuesserAnswerResponse submitAnswer(Authentication authentication,
                                                   PlaceGuesserAnswerRequest request, String sessionId) {

        PlaceGuesserSession session = getSessionAuthentication(authentication, sessionId);

        if (Boolean.TRUE.equals(session.getCompleted())) {
            throw new GameAlreadyCompletedException("Game has already completed");
        }

        int index = session.getCurrQuestionIndex();

        if (index < 0 || index >= session.getTotalQuestions()) {
            throw new OutOfRangeException("Invalid Question Index");
        }

        ImportantPlace correctPlace = placeRepo.findById(session.getImportantPlaceIds().get(index))
                .orElseThrow(() -> new NotFoundException("Place not found"));
        List<String> options = session.getCurrOptions();

        String answer = request.getAnswer() == null ? "" : request.getAnswer().trim();

        if (options == null || !options.stream()
                .anyMatch(option -> option.equalsIgnoreCase(answer))) {
            throw new IllegalArgumentException(
                    "Answer must be one of the available options");
        }

        boolean correct = correctPlace.getName() != null
                && correctPlace.getName().equalsIgnoreCase(answer);

        String message = correct ? "Correct Answer!" : "Wrong answer!";

        if (correct) {
            session.setScore(session.getScore() + 1);
        }

        session.setCurrQuestionIndex(index + 1);

        session.setCurrOptions(null);

        PlaceGuesserAnswerResponse response =
                new PlaceGuesserAnswerResponse();

        response.setCorrect(correct);
        response.setCorrectAnswer(correctPlace.getName());
        response.setScore(session.getScore());
        response.setMessage(message);

        if (session.getCurrQuestionIndex() >= session.getTotalQuestions()) {
            session.setCompleted(true);
            response.setCompleted(true);
            response.setNextQuestion(null);
        } else {
            response.setCompleted(false);
            response.setNextQuestion(buildCurrentQuestion(session));
        }

        try {
            placeGuesserRepo.save(session);
        } catch (OptimisticLockingFailureException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This question has already been answered or the game state has changed."
            );
        }

        return response;
    }

    public PlaceGuesserSummaryResponse getGameSummary(Authentication authentication, String sessionId){
        PlaceGuesserSession session=getSessionAuthentication(authentication,sessionId);

        PlaceGuesserSummaryResponse response=new PlaceGuesserSummaryResponse();
        response.setCompleted(session.getCompleted());
        response.setScore(session.getScore());
        response.setSessionId(session.getId());
        response.setTotalQuestions(session.getTotalQuestions());

        return response;
    }


    private PlaceGuesserQuestionResponse buildCurrentQuestion(PlaceGuesserSession session){
        String placeId=session.getImportantPlaceIds().get(session.getCurrQuestionIndex());
        ImportantPlace place=placeRepo.findById(placeId).
                orElseThrow(()->new NotFoundException("Place not found"));
        List<ImportantPlace> importantPlaces=placeRepo.findByPatientId(session.getPatientId());
        List<String> options=session.getCurrOptions();

        if(options==null || options.isEmpty()){
            List<String> placesName=new ArrayList<>(importantPlaces.stream().map(ImportantPlace::getName)
                    .filter(name->name!=null && !name.isBlank())
                    .distinct().filter(name->!name.equalsIgnoreCase(place.getName())).toList());
            Collections.shuffle(placesName);
            options=new ArrayList<>();
            options.addAll(placesName.stream().limit(MAX_OPTION-1).toList());
            options.add(place.getName());

            Collections.shuffle(options);
            session.setCurrOptions(options);
        }

        PlaceGuesserQuestionResponse response=new PlaceGuesserQuestionResponse();
        response.setOptions(options);
        response.setPlaceId(place.getId());
        response.setPhotoUrl(photoStorageService.photoUrl(place.getPhotoKey()));
        response.setQuestionNo(session.getCurrQuestionIndex()+1);
        response.setTotalQuestions(session.getTotalQuestions());
        return response;
    }

    private PlaceGuesserSession getSessionAuthentication(Authentication authentication
            ,String sessionId){
        Patient patient=patientRepo.findByUserId(authentication.getName())
                .orElseThrow(()->new NotFoundException("Patient not found"));
        PlaceGuesserSession session=placeGuesserRepo.findById(sessionId)
                .orElseThrow(()->new NotFoundException("Session not found"));
        if(!session.getPatientId().equals(patient.getId())){
            throw new UnauthorizedException("You dont have permission for this game session");
        }
        return session;
    }
}
