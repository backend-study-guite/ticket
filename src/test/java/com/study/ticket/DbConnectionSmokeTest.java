package com.study.ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DbConnectionSmokeTest {

    @Autowired
    DataSource dataSource;

    @Test
    void connect_to_test_db() throws Exception {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT DATABASE()")) {

            rs.next();
            String currentDb = rs.getString(1);

            System.out.println("✅ current db = " + currentDb);

            assertThat(currentDb).isEqualTo("ticket_test");
        }
    }
}
