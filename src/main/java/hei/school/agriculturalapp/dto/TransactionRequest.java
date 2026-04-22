package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TransactionRequest {
    private LocalDate from;
    private LocalDate to;
}