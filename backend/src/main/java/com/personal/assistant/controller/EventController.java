package com.personal.assistant.controller;

import com.personal.assistant.entity.JobEvent;
import com.personal.assistant.repository.JobEventRepository;
import com.personal.assistant.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = { "http://localhost:5173", "https://mail-automation-brown.vercel.app" }) // Allow React Frontend
public class EventController {

    @Autowired
    private JobEventRepository jobEventRepository;

    @Autowired
    private GmailService gmailService;

    @GetMapping
    public List<JobEvent> getAllEvents() {
        return jobEventRepository.findAll();
    }

    @PostMapping("/sync")
    public void syncEvents() {
        gmailService.fetchAndSaveNewEvents();
    }
}
