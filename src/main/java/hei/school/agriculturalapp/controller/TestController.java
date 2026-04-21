package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @GetMapping("/tests")
    public ResponseEntity<?> getTest(){
        try{
            return ResponseEntity.ok(testService.getTests());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
