package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.model.FinancialAccount;
import hei.school.agriculturalapp.repository.FinancialAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class FinancialAccountService {
    private final FinancialAccountRepository repository;

    public List<FinancialAccount> getAccountsByCollectivity(String collectivityId, LocalDate at) {
        LocalDate calculationDate = (at != null) ? at : LocalDate.now();
        return repository.findAccountsByCollectivityAtDate(collectivityId, calculationDate);
    }
}