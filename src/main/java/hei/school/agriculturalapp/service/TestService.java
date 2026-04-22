package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {
    private final DatabaseConfig dbconfig;

    public List<Test> getTests() {
        String query = "select * from test";
        List<Test> tests = new ArrayList<>();
        try (Connection c = dbconfig.connection();
             PreparedStatement pr = c.prepareStatement(query);
             ResultSet rs = pr.executeQuery();
                ){
            while (rs.next()) {
                Test t = new Test();
                t.setId(rs.getInt("id"));
                t.setName(rs.getString("name"));
                tests.add(t);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tests;
    }
}
