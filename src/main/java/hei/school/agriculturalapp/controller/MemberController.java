package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateMember;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> requests) {
        try {
            List<Member> members = memberService.createMembers(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(members);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}