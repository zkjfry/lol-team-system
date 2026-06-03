package com.example.lolteambackend.controller;

import com.example.lolteambackend.dto.AssignRequest;
import com.example.lolteambackend.dto.AssignResponse;
import com.example.lolteambackend.service.AssignmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/api/assign")
    public AssignResponse assign(@RequestBody AssignRequest request) {
        return assignmentService.assign(request);
    }
}