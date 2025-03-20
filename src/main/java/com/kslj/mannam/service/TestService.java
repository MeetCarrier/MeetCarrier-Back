package com.kslj.mannam.service;

import com.kslj.mannam.entity.Test;
import com.kslj.mannam.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;

    public void addHello() {
        testRepository.save(Test.builder().name("hello").build());
    }
}
