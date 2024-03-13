package edu.ucsb.cs156.happiercows.controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import edu.ucsb.cs156.happiercows.ControllerTestCase;
import edu.ucsb.cs156.happiercows.repositories.AnnouncementsRepository;
import edu.ucsb.cs156.happiercows.entities.Announcements;

import edu.ucsb.cs156.happiercows.repositories.UserCommonsRepository;
import edu.ucsb.cs156.happiercows.entities.UserCommons;
import edu.ucsb.cs156.happiercows.entities.Commons;
import edu.ucsb.cs156.happiercows.entities.CommonsPlus;
import edu.ucsb.cs156.happiercows.services.CommonsPlusBuilderService;
import edu.ucsb.cs156.happiercows.repositories.CommonsRepository;
import edu.ucsb.cs156.happiercows.strategies.CowHealthUpdateStrategies;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(controllers = AnnouncementsController.class)
@Import(AnnouncementsController.class)
@AutoConfigureDataJpa
public class AnnouncementsControllerTests extends ControllerTestCase {

    @MockBean
    AnnouncementsRepository announcementsRepository;

    @MockBean
    UserCommonsRepository userCommonsRepository;

    @MockBean
    CommonsRepository commonsRepository;

    @Autowired
    ObjectMapper mapper;


    //* */ post tests
    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void createAnnouncementsTest() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(announcementsRepository, atLeastOnce()).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Added announcement to commons with id %d", commonsId);
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userNotInCommonsCannotPost() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isForbidden()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void cannotPostWithoutStart() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));

        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&announcement={announcement}&start=", commonsId, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void endCannotBeBeforeStart() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-04T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("End time must be after start time");
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void announcementCannotBeEmpty() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-06T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Announcement cannot be empty");
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userCommonsCanPost() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).announcement(announcement).build();

        when(announcementsRepository.save(any(Announcements.class))).thenReturn(announcements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.of(userCommons));

        //act 
        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&announcement={announcement}", commonsId, start, announcement).with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(announcementsRepository, atLeastOnce()).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Added announcement to commons with id %d", commonsId);
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void cannotPostIfCommonsInvalid() throws Exception {
        Long commonsId = 1L;
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-06T15:50:10");

        when(commonsRepository.findById(commonsId)).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(post("/api/announcements/post?commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Commons with id %d not found", commonsId);
        assertEquals(expectedResponseString, responseString);
    }

    // Get tests
    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void getAllAnnouncementsTest() throws Exception {
        List<Announcements> expectedAnnouncements = new ArrayList<Announcements>();
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements1 = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();
        Announcements announcements2 = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();
        expectedAnnouncements.add(announcements1);
        expectedAnnouncements.add(announcements2);

        when(announcementsRepository.findAllByCommonsId(commonsId)).thenReturn(expectedAnnouncements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(get("/api/announcements/get?commonsId={commonsId}", commonsId).with(csrf()))
            .andExpect(status().isOk()).andReturn();
        

        // assert
        
        String announcementString = response.getResponse().getContentAsString();
        List<Announcements> actualAnnouncements = mapper.readValue(announcementString, new TypeReference<List<Announcements>>() {
        });
        assertEquals(actualAnnouncements, expectedAnnouncements);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userInCommonsCanGetAll() throws Exception {
        List<Announcements> expectedAnnouncements = new ArrayList<Announcements>();
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements1 = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();
        Announcements announcements2 = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();
        expectedAnnouncements.add(announcements1);
        expectedAnnouncements.add(announcements2);

        when(announcementsRepository.findAllByCommonsId(commonsId)).thenReturn(expectedAnnouncements);

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.of(userCommons));

        //act 
        MvcResult response = mockMvc.perform(get("/api/announcements/get?commonsId={commonsId}", commonsId).with(csrf()))
            .andExpect(status().isOk()).andReturn();
        

        // assert
        
        String announcementString = response.getResponse().getContentAsString();
        List<Announcements> actualAnnouncements = mapper.readValue(announcementString, new TypeReference<List<Announcements>>() {
        });
        assertEquals(actualAnnouncements, expectedAnnouncements);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void getByIdTest() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements1 = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(commonsId)).thenReturn(Optional.of(announcements1));

        //act 
        MvcResult response = mockMvc.perform(get("/api/announcements/get/by-id?announcementId={announcementId}", id).with(csrf()))
            .andExpect(status().isOk()).andReturn();
        

        // assert
        
        String announcementString = response.getResponse().getContentAsString();
        String expectedAnnouncements = mapper.writeValueAsString(announcements1);
        assertEquals(announcementString, expectedAnnouncements);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void cannotGetNonexistentAnnouncementById() throws Exception {
        Long id = 0L;
        when(announcementsRepository.findById(id)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(get("/api/announcements/get/by-id?announcementId={announcementId}", id).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();
    }

    // Delete Tests
    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void deleteAnnouncementsTest() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        // act 
        MvcResult response = mockMvc.perform(delete("/api/announcements?announcementId={announcementId}", id).with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(announcementsRepository, atLeastOnce()).delete(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("{\"message\":\"announcement with id 0 deleted\"}", id);
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userCannotDelete() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        // act 
        MvcResult response = mockMvc.perform(delete("/api/announcements?announcementId={announcementId}", id).with(csrf()))
            .andExpect(status().isForbidden()).andReturn();

        verify(announcementsRepository, times(0)).delete(any(Announcements.class));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void cannotDeleteNonexistentAnnouncement() throws Exception {
    
        Long id = 0L;

        when(announcementsRepository.findById(id)).thenReturn(Optional.empty());

        // act 
        MvcResult response = mockMvc.perform(delete("/api/announcements?announcementId={announcementId}", id).with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(announcementsRepository, times(0)).delete(any(Announcements.class));
    }

    // Put Tests
    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void updateAnnouncementsTest() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        Long commonsId2 = 10L;
        String announcement2 = "test2";
        LocalDateTime start2 = LocalDateTime.parse("2022-03-07T15:50:10");
        LocalDateTime end2 = LocalDateTime.parse("2022-03-08T15:50:10");
        when(commonsRepository.findById(10L)).thenReturn(Optional.of(commons));

        // act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", 
        id, commonsId2, start2, end2, announcement2).with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(announcementsRepository, atLeastOnce()).save(any(Announcements.class));
        assertEquals(announcements.getCommonsId(), commonsId2);
        assertEquals(announcements.getStart(), start2);
        assertEquals(announcements.getEnd(), end2);
        assertEquals(announcements.getAnnouncement(), announcement2);
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Updated announcement with id %d", id);
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userNotInCommonsCannotUpdate() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", id, commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isForbidden()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void cannotUpdateWithoutStart() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));

        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&announcement={announcement}&start=", id, commonsId, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void endCannotBeBeforeStartUpdate() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-04T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", id, commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("End time must be after start time");
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void announcementCannotBeEmptyUpdate() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-06T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.empty());

        //act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", id, commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Announcement cannot be empty");
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void cannotUpdateIfCommonsInvalid() throws Exception {
        Long commonsId = 1L;
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");
        LocalDateTime end = LocalDateTime.parse("2022-03-06T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).end(end).announcement(announcement).build();

        when(commonsRepository.findById(commonsId)).thenReturn(Optional.empty());
        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));

        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&end={end}&announcement={announcement}", id, commonsId, start, end, announcement).with(csrf()))
            .andExpect(status().isBadRequest()).andReturn();

        // assert
        verify(announcementsRepository, times(0)).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Commons with id %d not found", commonsId);
        assertEquals(expectedResponseString, responseString);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void userCommonsCanUpdate() throws Exception {
        LocalDateTime someTime = LocalDateTime.parse("2022-03-05T15:50:10");

        Commons commons = Commons.builder().build();
        when(commonsRepository.findById(commons.getId())).thenReturn(Optional.of(commons));
        
        Long commonsId = commons.getId();
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");

        Announcements announcements = Announcements.builder().id(id).commonsId(commonsId).start(start).announcement(announcement).build();

        when(announcementsRepository.findById(id)).thenReturn(Optional.of(announcements));
        UserCommons userCommons = UserCommons.builder().build();
        when(userCommonsRepository.findByCommonsIdAndUserId(commonsId, userId)).thenReturn(Optional.of(userCommons));

        String announcement2 = "test2";
        LocalDateTime start2 = LocalDateTime.parse("2022-03-07T15:50:10");
        LocalDateTime end2 = LocalDateTime.parse("2022-03-08T15:50:10");

        // act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&announcement={announcement}", 
        id, commonsId, start2, announcement2).with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(announcementsRepository, atLeastOnce()).save(any(Announcements.class));
        String responseString = response.getResponse().getContentAsString();
        String expectedResponseString = String.format("Updated announcement with id %d", id);
        assertEquals(expectedResponseString, responseString);
    }


    @WithMockUser(roles = {"USER"})
    @Test
    public void cannotUpdateNonexistentAnnouncement() throws Exception {
    
        Long commonsId = 1L;
        Long id = 0L;
        Long userId = 1L;
        String announcement = "test";
        LocalDateTime start = LocalDateTime.parse("2022-03-05T15:50:10");

        when(announcementsRepository.findById(id)).thenReturn(Optional.empty());

        // act 
        MvcResult response = mockMvc.perform(put("/api/announcements?announcementId={announcementId}&commonsId={commonsId}&start={start}&announcement={announcement}", id, commonsId, start, announcement).with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(announcementsRepository, times(0)).delete(any(Announcements.class));
    }
}