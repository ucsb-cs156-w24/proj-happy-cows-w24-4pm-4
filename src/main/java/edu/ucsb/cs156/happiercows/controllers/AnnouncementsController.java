package edu.ucsb.cs156.happiercows.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import edu.ucsb.cs156.happiercows.entities.Announcements;
import edu.ucsb.cs156.happiercows.repositories.AnnouncementsRepository;

import edu.ucsb.cs156.happiercows.entities.User;
import edu.ucsb.cs156.happiercows.entities.UserCommons;
import edu.ucsb.cs156.happiercows.repositories.UserCommonsRepository;
import edu.ucsb.cs156.happiercows.repositories.CommonsRepository;

import org.springframework.security.core.Authentication;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Tag(name = "Announcements")
@RequestMapping("/api/announcements")
@RestController
@Slf4j
public class AnnouncementsController extends ApiController {
    @Autowired
    CommonsRepository commonsRepository;

    @Autowired
    UserCommonsRepository userCommonsRepository;

    @Autowired
    AnnouncementsRepository announcementsRepository;

    @Autowired
    ObjectMapper mapper;

    @Operation(summary = "Get all announcements", description = "Get all announcements associated with a specific commons.")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<String> getAnnouncements(@Parameter(description = "The id of the common") @RequestParam Long commonsId) throws JsonProcessingException {

        // Return the list of announcements
        Iterable<Announcements> messages = announcementsRepository.findAllByCommonsId(commonsId);
        String body = mapper.writeValueAsString(messages);
        return ResponseEntity.ok().body(body);
    }

    @Operation(summary = "Get announcement by id", description = "Get announcement associated with a specific id.")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/get/by-id")
    public ResponseEntity<String> getAnnouncementsById(@Parameter(description = "The id of the announcement") @RequestParam Long announcementId) throws JsonProcessingException {

        // Try to get the announcement
        Optional<Announcements> announcementLookup = announcementsRepository.findById(announcementId);
        if (!announcementLookup.isPresent()) {
            String responseString = String.format("Announcement with id %d not found", announcementId);
            return ResponseEntity.badRequest().body(responseString);
        }

        // Return the announcement
        String body = mapper.writeValueAsString(announcementLookup);
        return ResponseEntity.ok().body(body);
    }  
    
    @Operation(summary = "Create an announcement", description = "Create an announcement associated with a specific commons")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/post")
    public ResponseEntity<String> createAnnouncements(@Parameter(description = "The id of the common") @RequestParam Long commonsId,
                                            @Parameter(name="start", description="Start time (in iso format, e.g. YYYY-mm-ddTHH:MM:SS) ex: 2024-03-06T04:26:58.76") @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                            @Parameter(name="end", description="End time (in iso format)") @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                            @Parameter(name="announcement") @RequestParam String announcement) {
        
        User user = getCurrentUser().getUser();
        Long userId = user.getId();

        // Make sure the commons exists
        if (!commonsRepository.findById(commonsId).isPresent()) {
            String responseString = String.format("Commons with id %d not found", commonsId);
            return ResponseEntity.badRequest().body(responseString);
        }

        // Make sure the user is part of the commons or is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            log.info("User is not an admin");
            Optional<UserCommons> userCommonsLookup = userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId);

            if (!userCommonsLookup.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Ensure start date is before end date
        if (end != null) {
            if (end.isBefore(start)) {
                return ResponseEntity.badRequest().body("End time must be after start time");
            }
        }

        // Check if announcements is empty
        if (announcement.isEmpty()) {
            return ResponseEntity.badRequest().body("Announcement cannot be empty");
        }

        // Create the announcement
        Announcements announcements = Announcements.builder()
        .commonsId(commonsId)
        .start(start)
        .end(end)
        .announcement(announcement)
        .build();

        // Save the announcement
        announcementsRepository.save(announcements);
        String responseString = String.format("Added announcement to commons with id %d", commonsId);

        return ResponseEntity.ok().body(responseString);
    }

    @Operation(summary = "Delete an announcement", description = "Delete an announcement associated with a specific commons")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteAnnouncements(
        @Parameter(description = "The id of the announcement") @RequestParam Long announcementId) {

        // Try to get the announcement
        Optional<Announcements> announcementLookup = announcementsRepository.findById(announcementId);
        if (!announcementLookup.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Announcements announcement = announcementLookup.get();

        User user = getCurrentUser().getUser();
        Long userId = user.getId();

        // Check if the user is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delete the announcement
        announcementsRepository.delete(announcement);
        String responseString = String.format("announcement with id %d deleted", announcementId);
        return genericMessage(responseString);
    }

    @Operation(summary = "Update an announcement", description = "Update an announcement associated with a specific id")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("")
    public ResponseEntity<String> updateAnnouncements(@Parameter(description = "The id of the announcement") @RequestParam Long announcementId,
                                            @Parameter(description = "The id of the common") @RequestParam Long commonsId,
                                            @Parameter(name="start", description="Start time (in iso format, e.g. YYYY-mm-ddTHH:MM:SS) ex: 2024-03-06T04:26:58.76") @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                            @Parameter(name="end", description="End time (in iso format)") @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                            @Parameter(name="announcement") @RequestParam String announcement) {
        
        User user = getCurrentUser().getUser();
        Long userId = user.getId();

        // Try to get the announcement
        Optional<Announcements> exists = announcementsRepository.findById(announcementId);
        if (!exists.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Announcements announcements = exists.get();

        // Make sure the commons exists
        if (!commonsRepository.findById(commonsId).isPresent()) {
            String responseString = String.format("Commons with id %d not found", commonsId);
            return ResponseEntity.badRequest().body(responseString);
        }

        // Make sure the user is part of the commons or is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            log.info("User is not an admin");
            Optional<UserCommons> userCommonsLookup = userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId);

            if (!userCommonsLookup.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Ensure start date is before end date
        if (end != null) {
            if (end.isBefore(start)) {
                return ResponseEntity.badRequest().body("End time must be after start time");
            }
        }

        // Check if announcements is empty
        if (announcement.isEmpty()) {
            return ResponseEntity.badRequest().body("Announcement cannot be empty");
        }

        // Update the announcement
        announcements.setCommonsId(commonsId);
        announcements.setStart(start);
        announcements.setEnd(end);
        announcements.setAnnouncement(announcement);

        // Save the announcement
        announcementsRepository.save(announcements);
        String responseString = String.format("Updated announcement with id %d", announcementId);
        return ResponseEntity.ok().body(responseString);
    }
}