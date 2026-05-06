package hei.school.agriculturalapp.service;

public interface AccountStrategy {
    boolean supports(String paymentMode);
    void credit(String accountIdentifier, Integer amount);
}