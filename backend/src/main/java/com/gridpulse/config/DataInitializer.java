package com.gridpulse.config;

import com.gridpulse.entity.*;
import com.gridpulse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubstationRepository substationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private RepairHistoryRepository repairHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Users
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@gridpulse.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Systems Administrator")
                    .role("ROLE_ADMIN")
                    .build();

            User operator = User.builder()
                    .username("operator")
                    .email("operator@gridpulse.com")
                    .password(passwordEncoder.encode("operator123"))
                    .fullName("Grid Operator One")
                    .role("ROLE_OPERATOR")
                    .build();

            User tech = User.builder()
                    .username("technician")
                    .email("tech@gridpulse.com")
                    .password(passwordEncoder.encode("technician123"))
                    .fullName("Jane Smith (Technician)")
                    .role("ROLE_TECHNICIAN")
                    .build();

            userRepository.saveAll(Arrays.asList(admin, operator, tech));
            System.out.println("Seeded default users: admin/admin123, operator/operator123, technician/technician123");
        }

        // 2. Seed Substations (with coordinates around Bangalore or any default city)
        if (substationRepository.count() == 0) {
            Substation subA = Substation.builder()
                    .name("Metro Grid Substation A")
                    .location("Central Business District, Sector 4")
                    .latitude(12.9716)
                    .longitude(77.5946)
                    .status("HEALTHY")
                    .maxCapacityKw(5000.0)
                    .description("Serves commercial offices and transit systems.")
                    .build();

            Substation subB = Substation.builder()
                    .name("Industrial Hub Substation B")
                    .location("Peenya Industrial Area Phase 2")
                    .latitude(12.9800)
                    .longitude(77.6000)
                    .status("HEALTHY")
                    .maxCapacityKw(12000.0)
                    .description("Serves heavy manufacturing and logistics warehouses.")
                    .build();

            Substation subC = Substation.builder()
                    .name("Residential Hub Substation C")
                    .location("Jayanagar Residential Block 5")
                    .latitude(12.9650)
                    .longitude(77.5850)
                    .status("HEALTHY")
                    .maxCapacityKw(3500.0)
                    .description("Serves high-density housing developments and retail blocks.")
                    .build();

            Substation subD = Substation.builder()
                    .name("Downtown Commercial Substation D")
                    .location("Indiranagar 100 Feet Rd")
                    .latitude(12.9900)
                    .longitude(77.5900)
                    .status("HEALTHY")
                    .maxCapacityKw(7500.0)
                    .description("Serves shopping centers, hospitals, and entertainment zones.")
                    .build();

            Substation subE = Substation.builder()
                    .name("West Grid Substation E")
                    .location("Whitefield IT Park Lane 2")
                    .latitude(12.9550)
                    .longitude(77.6100)
                    .status("HEALTHY")
                    .maxCapacityKw(10000.0)
                    .description("Serves large corporate campuses and server hosting hubs.")
                    .build();

            substationRepository.saveAll(Arrays.asList(subA, subB, subC, subD, subE));
            System.out.println("Seeded 5 grid substations.");
        }

        // 3. Seed Technicians
        if (technicianRepository.count() == 0) {
            Technician tech1 = Technician.builder()
                    .name("John Doe")
                    .skills("Transformer Maintenance, Substation Automation")
                    .availability("AVAILABLE")
                    .currentJobs(0)
                    .phone("+91-9876543210")
                    .build();

            Technician tech2 = Technician.builder()
                    .name("Jane Smith")
                    .skills("Cable Repair, High Voltage Breakers")
                    .availability("AVAILABLE")
                    .currentJobs(0)
                    .phone("+91-8765432109")
                    .build();

            Technician tech3 = Technician.builder()
                    .name("David Miller")
                    .skills("Grid Protection Systems, Telecom Diagnostics")
                    .availability("AVAILABLE")
                    .currentJobs(0)
                    .phone("+91-7654321098")
                    .build();

            technicianRepository.saveAll(Arrays.asList(tech1, tech2, tech3));
            System.out.println("Seeded 3 technicians.");
        }

        // 4. Seed Customers
        if (customerRepository.count() == 0) {
            Customer cust1 = Customer.builder()
                    .name("Alice Johnson")
                    .email("alice.j@example.com")
                    .phone("+91-9988776655")
                    .address("Jayanagar 4th Block, #120")
                    .accountNumber("GP-1001")
                    .status("ACTIVE")
                    .averageConsumptionKwh(340.5)
                    .build();

            Customer cust2 = Customer.builder()
                    .name("Vertex Tech Corp")
                    .email("facilities@vertextech.com")
                    .phone("+91-8877665544")
                    .address("Whitefield IT Zone, Building 4B")
                    .accountNumber("GP-1002")
                    .status("ACTIVE")
                    .averageConsumptionKwh(8450.0)
                    .build();

            Customer cust3 = Customer.builder()
                    .name("Bob Smith")
                    .email("bob.smith@example.com")
                    .phone("+91-7766554433")
                    .address("Indiranagar 12th Cross, #45")
                    .accountNumber("GP-1003")
                    .status("ACTIVE")
                    .averageConsumptionKwh(410.2)
                    .build();

            customerRepository.saveAll(Arrays.asList(cust1, cust2, cust3));
            System.out.println("Seeded default customers.");
        }

        // 5. Seed Repair History for AI Context
        if (repairHistoryRepository.count() == 0) {
            RepairHistory hist1 = RepairHistory.builder()
                    .substationId(1L)
                    .substationName("Metro Grid Substation A")
                    .faultResolved("Transformer winding replacement")
                    .technicianName("John Doe")
                    .completedAt(LocalDateTime.now().minusMonths(9))
                    .notes("Transformer overheated due to winding insulation failure. Replaced primary winding.")
                    .build();

            RepairHistory hist2 = RepairHistory.builder()
                    .substationId(1L)
                    .substationName("Metro Grid Substation A")
                    .faultResolved("High-voltage cable splice repair")
                    .technicianName("Jane Smith")
                    .completedAt(LocalDateTime.now().minusYears(2))
                    .notes("Underground feeder cable short circuit. Spliced section between terminal A and pole.")
                    .build();

            RepairHistory hist3 = RepairHistory.builder()
                    .substationId(2L)
                    .substationName("Industrial Hub Substation B")
                    .faultResolved("Cooling system pump renewal")
                    .technicianName("John Doe")
                    .completedAt(LocalDateTime.now().minusMonths(3))
                    .notes("Substation cooling fluid pump seized. Replaced with new brushless pump motor.")
                    .build();

            repairHistoryRepository.saveAll(Arrays.asList(hist1, hist2, hist3));
            System.out.println("Seeded repair logs for AI historical diagnostic retrieval.");
        }
    }
}
