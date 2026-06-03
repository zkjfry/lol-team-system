package com.example.lolteambackend.service;

import com.example.lolteambackend.dto.AssignRequest;
import com.example.lolteambackend.dto.AssignResponse;

public interface AssignmentService {

    AssignResponse assign(AssignRequest request);
}