package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.model.CollectivityTransaction;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.CollectivityTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectivityTransactionService {

    private final CollectivityTransactionRepository transactionRepository;
    private final CollectivityRepository collectivityRepository;

    public List<CollectivityTransaction> getTransactions(
            String collectivityId, LocalDate from, LocalDate to) throws SQLException {

        if (!collectivityRepository.existsById(collectivityId)) {
            throw new IllegalArgumentException("Collectivity not found");
        }

        if (from == null || to == null) {
            throw new IllegalArgumentException("Query parameters 'from' and 'to' are required");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before or equal to 'to' date");
        }

        return transactionRepository.findByCollectivityIdAndDateRange(collectivityId, from, to);
    }
}