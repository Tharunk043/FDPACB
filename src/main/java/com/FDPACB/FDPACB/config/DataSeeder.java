package com.FDPACB.FDPACB.config;

import com.FDPACB.FDPACB.entity.FinancialRecord;
import com.FDPACB.FDPACB.entity.User;
import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import com.FDPACB.FDPACB.enums.Role;
import com.FDPACB.FDPACB.repository.FinancialRecordRepository;
import com.FDPACB.FDPACB.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      FinancialRecordRepository recordRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: data already present, skipping seed.");
            return;
        }

        log.info("DataSeeder: seeding default users and sample financial records...");

        // ─── Seed Users ───────────────────────────────────────────────────────
        User admin = new User("admin", "admin@fdpacb.dev",
                passwordEncoder.encode("admin123"), Role.ADMIN);
        User analyst = new User("analyst", "analyst@fdpacb.dev",
                passwordEncoder.encode("analyst123"), Role.ANALYST);
        User viewer = new User("viewer", "viewer@fdpacb.dev",
                passwordEncoder.encode("viewer123"), Role.VIEWER);

        userRepository.save(admin);
        userRepository.save(analyst);
        userRepository.save(viewer);

        // ─── Seed Financial Records ───────────────────────────────────────────
        LocalDate now = LocalDate.now();

        Object[][] records = {
            // { amount, type, category, daysAgo, notes }
            { "85000.00",  RecordType.INCOME,  Category.SALARY,        0,  "Monthly salary - April 2025" },
            { "15000.00",  RecordType.INCOME,  Category.INVESTMENT,    2,  "Dividend payout from mutual fund" },
            { "5000.00",   RecordType.INCOME,  Category.OTHER,         5,  "Freelance project payment" },
            { "3200.00",   RecordType.EXPENSE, Category.FOOD,          1,  "Monthly groceries" },
            { "1500.00",   RecordType.EXPENSE, Category.TRANSPORT,     3,  "Fuel & cab expenses" },
            { "2800.00",   RecordType.EXPENSE, Category.UTILITIES,     4,  "Electricity + Internet bill" },
            { "4500.00",   RecordType.EXPENSE, Category.ENTERTAINMENT, 6,  "Streaming subscriptions + dining out" },
            { "12000.00",  RecordType.EXPENSE, Category.HEALTHCARE,    8,  "Health insurance premium" },
            { "78000.00",  RecordType.INCOME,  Category.SALARY,       30,  "Monthly salary - March 2025" },
            { "8000.00",   RecordType.INCOME,  Category.INVESTMENT,   32,  "Stock sale proceeds" },
            { "3100.00",   RecordType.EXPENSE, Category.FOOD,         31,  "March grocery run" },
            { "1200.00",   RecordType.EXPENSE, Category.TRANSPORT,    35,  "Monthly commute pass" },
            { "3000.00",   RecordType.EXPENSE, Category.UTILITIES,    33,  "March utility bills" },
            { "78000.00",  RecordType.INCOME,  Category.SALARY,       60,  "Monthly salary - February 2025" },
            { "9500.00",   RecordType.EXPENSE, Category.HEALTHCARE,   62,  "Dental treatment" },
        };

        for (Object[] r : records) {
            FinancialRecord rec = new FinancialRecord();
            rec.setAmount(new BigDecimal((String) r[0]));
            rec.setType((RecordType) r[1]);
            rec.setCategory((Category) r[2]);
            rec.setDate(now.minusDays((int) r[3]));
            rec.setNotes((String) r[4]);
            rec.setCreatedBy(admin);
            recordRepository.save(rec);
        }

        log.info("DataSeeder: seeded 3 users and {} financial records.", records.length);
    }
}
