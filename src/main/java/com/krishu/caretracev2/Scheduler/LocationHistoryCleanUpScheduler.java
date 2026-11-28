package com.krishu.caretracev2.Scheduler;

import com.krishu.caretracev2.Repository.LocationEventRepo;
import com.krishu.caretracev2.Repository.LocationHistoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LocationHistoryCleanUpScheduler {

    private static final Logger log = LoggerFactory.
            getLogger(LocationHistoryCleanUpScheduler.class);

    private final LocationHistoryRepo locationHistoryRepo;
    private final LocationEventRepo locationEventRepo;

    @Value("${location.history.retention.days}")
    private Integer retentionDays;

    public LocationHistoryCleanUpScheduler(LocationHistoryRepo locationHistoryRepo,
                                           LocationEventRepo locationEventRepo){
        this.locationHistoryRepo=locationHistoryRepo;
        this.locationEventRepo = locationEventRepo;
    }

    @Scheduled(cron="0 30 3 * * *")
    public void cleanUpHistory(){
        try{
            long deletedRecords=locationHistoryRepo.
                    deleteByRecordedAtBefore(LocalDateTime.now().minusDays(retentionDays));
            locationEventRepo.deleteByOccurredAtBefore(LocalDateTime.now().minusDays(retentionDays));
            log.info("Deleted {} old location history points",deletedRecords);
        }catch(RuntimeException e){
            log.warn("Could not clean up location history: {}",e.getMessage());
        }
    }
}
