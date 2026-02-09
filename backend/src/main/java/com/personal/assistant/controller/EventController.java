package com.personal.assistant.controller;

import com.personal.assistant.entity.JobEvent;
import com.personal.assistant.repository.JobEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173") // Allow React Frontend
public class EventController {

    @Autowired
    private JobEventRepository jobEventRepository;

    @GetMapping
    public List<JobEvent> getEvents() {
        return jobEventRepository.findAll();
    }
}
