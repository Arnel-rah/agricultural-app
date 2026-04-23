package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.MemberPayment;
import java.sql.SQLException;
import java.util.List;

public interface MemberPaymentService {
    List<MemberPayment> savePayments(String memberId, List<CreateMemberPayment> requests) throws SQLException, BadRequestException;
}