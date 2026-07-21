package com.gridpulse.config;

import com.gridpulse.entity.*;
import com.gridpulse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String JOHN_DOE = "john_doe";
    private static final String JANE_SMITH = "jane_smith";
    private static final String DAVID_MILLER = "david_miller";
    private static final String NAME_JOHN_DOE = "John Doe";
    private static final String NAME_JANE_SMITH = "Jane Smith";
    private static final String NAME_DAVID_MILLER = "David Miller";
    private static final String TEMP_PASSWORD = "tempPassword123";
    private static final String ROLE_TECHNICIAN = "TECHNICIAN";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_UPPER_ACTIVE = "ACTIVE";

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
        
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@gridpulse.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Systems Administrator")
                    .role("ADMIN")
                    .passwordChanged(true)
                    .build();

            User operator = User.builder()
                    .username("operator")
                    .email("operator@gridpulse.com")
                    .password(passwordEncoder.encode("Operator@123"))
                    .fullName("Grid Operator One")
                    .role("GRID_OPERATOR")
                    .passwordChanged(true)
                    .build();

            User uTech1 = User.builder()
                    .username(JOHN_DOE)
                    .email("john@gridpulse.com")
                    .password(passwordEncoder.encode(TEMP_PASSWORD))
                    .fullName(NAME_JOHN_DOE)
                    .role(ROLE_TECHNICIAN)
                    .passwordChanged(false)
                    .build();

            User uTech2 = User.builder()
                    .username(JANE_SMITH)
                    .email("jane@gridpulse.com")
                    .password(passwordEncoder.encode(TEMP_PASSWORD))
                    .fullName(NAME_JANE_SMITH)
                    .role(ROLE_TECHNICIAN)
                    .passwordChanged(false)
                    .build();

            User uTech3 = User.builder()
                    .username(DAVID_MILLER)
                    .email("david@gridpulse.com")
                    .password(passwordEncoder.encode(TEMP_PASSWORD))
                    .fullName(NAME_DAVID_MILLER)
                    .role(ROLE_TECHNICIAN)
                    .passwordChanged(false)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, operator, uTech1, uTech2, uTech3));
            log.info("Seeded default users: admin/Admin@123, operator/Operator@123, technicians/tempPassword123");
        }


        
        if (substationRepository.count() == 0) {
            class StateGridData {
                String name;
                String location;
                double lat;
                double lon;
                double capacity;
                String desc;

                StateGridData(String name, String location, double lat, double lon, double capacity, String desc) {
                    this.name = name;
                    this.location = location;
                    this.lat = lat;
                    this.lon = lon;
                    this.capacity = capacity;
                    this.desc = desc;
                }
            }

            java.util.List<StateGridData> states = Arrays.asList(
                new StateGridData("Andhra Pradesh Grid", "Amaravati, Andhra Pradesh", 16.5062, 80.6480, 15000.0, "Serves South-Central grid region, farming districts, and maritime ports."),
                new StateGridData("Arunachal Pradesh Grid", "Itanagar, Arunachal Pradesh", 27.0844, 93.6053, 5000.0, "Serves Eastern Himalayan borders, forest zones, and hydro stations."),
                new StateGridData("Assam Grid", "Guwahati, Assam", 26.1445, 91.7362, 10000.0, "Serves North-East tea production, oil refineries, and river networks."),
                new StateGridData("Bihar Grid", "Patna, Bihar", 25.5941, 85.1376, 12000.0, "Serves high-density agricultural belts, heritage sites, and river basins."),
                new StateGridData("Chhattisgarh Grid", "Raipur, Chhattisgarh", 21.2514, 81.6296, 11000.0, "Serves major steel plants, mineral reserves, and power generation hubs."),
                new StateGridData("Goa Grid", "Panaji, Goa", 15.4909, 73.8278, 6000.0, "Serves coastal tourism zones, iron ore mines, and shipping terminals."),
                new StateGridData("Gujarat Grid", "Gandhinagar, Gujarat", 23.2156, 72.6369, 22000.0, "Serves solar parks, textile mills, chemical zones, and manufacturing."),
                new StateGridData("Haryana Grid", "Gurugram, Haryana", 28.4595, 77.0266, 18000.0, "Serves industrial corridors, corporate IT parks, and automotive plants."),
                new StateGridData("Himachal Pradesh Grid", "Shimla, Himachal Pradesh", 31.1048, 77.1734, 7000.0, "Serves mountain tourism, apple orchards, and hydroelectric basins."),
                new StateGridData("Jharkhand Grid", "Ranchi, Jharkhand", 23.3441, 85.3096, 11000.0, "Serves heavy metal mines, steel manufacturing, and industrial cities."),
                new StateGridData("Karnataka Grid", "Bengaluru, Karnataka", 12.9716, 77.5946, 20000.0, "Serves tech parks, space exploration, and heavy aviation sectors."),
                new StateGridData("Kerala Grid", "Thiruvananthapuram, Kerala", 8.5241, 76.9366, 12000.0, "Serves coastal tourism, spices, marine processing, and digital grids."),
                new StateGridData("Madhya Pradesh Grid", "Bhopal, Madhya Pradesh", 23.2599, 77.4126, 16000.0, "Serves central agricultural zones, forest grids, and textile belts."),
                new StateGridData("Maharashtra Grid", "Mumbai, Maharashtra", 19.0760, 72.8777, 30000.0, "Serves financial districts, film studios, shipping, and heavy manufacturing."),
                new StateGridData("Manipur Grid", "Imphal, Manipur", 24.8170, 93.9368, 4000.0, "Serves border trade pathways, farming, and ecological reserves."),
                new StateGridData("Meghalaya Grid", "Shillong, Meghalaya", 25.5788, 91.8831, 4500.0, "Serves mining grids, tourism, and high-precipitation hydro setups."),
                new StateGridData("Mizoram Grid", "Aizawl, Mizoram", 23.7307, 92.7173, 4000.0, "Serves border transit routes, bamboo farming, and solar installations."),
                new StateGridData("Nagaland Grid", "Kohima, Nagaland", 25.6751, 94.1086, 4000.0, "Serves mountain farming, timber industries, and micro-grid setups."),
                new StateGridData("Odisha Grid", "Bhubaneswar, Odisha", 20.2961, 85.8245, 14000.0, "Serves iron & steel plants, space test centers, and coastal ports."),
                new StateGridData("Punjab Grid", "Amritsar, Punjab", 31.6340, 74.8723, 15000.0, "Serves massive food grain hubs, textiles, and border trade networks."),
                new StateGridData("Rajasthan Grid", "Jaipur, Rajasthan", 26.9124, 75.7873, 17000.0, "Serves solar setups, marble mines, tourism, and desert border grids."),
                new StateGridData("Sikkim Grid", "Gangtok, Sikkim", 27.3389, 88.6065, 5000.0, "Serves organic farming hubs, tourism, and mountain hydro stations."),
                new StateGridData("Tamil Nadu Grid", "Chennai, Tamil Nadu", 13.0827, 80.2707, 24000.0, "Serves automotive manufacturing, software corridors, and shipping hubs."),
                new StateGridData("Telangana Grid", "Hyderabad, Telangana", 17.3850, 78.4867, 20000.0, "Serves biotechnology parks, software campuses, and pharmaceutical hubs."),
                new StateGridData("Tripura Grid", "Agartala, Tripura", 23.8315, 91.2868, 5000.0, "Serves natural gas grids, rubber processing, and border channels."),
                new StateGridData("Uttar Pradesh Grid", "Lucknow, Uttar Pradesh", 26.8467, 80.9462, 28000.0, "Serves high-density cities, sugar mills, and major heritage networks."),
                new StateGridData("Uttarakhand Grid", "Dehradun, Uttarakhand", 30.3165, 78.0322, 9000.0, "Serves hydro grids, mountain tourism, and industrial estates."),
                new StateGridData("West Bengal Grid", "Kolkata, West Bengal", 22.5726, 88.3639, 18000.0, "Serves eastern ports, coal mining borders, and heavy steel plants.")
            );

            java.util.List<Substation> substations = new java.util.ArrayList<>();
            for (StateGridData s : states) {
                substations.add(Substation.builder()
                        .name(s.name)
                        .location(s.location)
                        .latitude(s.lat)
                        .longitude(s.lon)
                        .status("HEALTHY")
                        .maxCapacityKw(s.capacity)
                        .description(s.desc)
                        .build());
            }

            substationRepository.saveAll(substations);
            log.info("Seeded all 28 Indian state grid substations successfully.");
        }


        
        if (technicianRepository.count() == 0) {
            User uTech1 = userRepository.findByUsername(JOHN_DOE).orElse(null);
            User uTech2 = userRepository.findByUsername(JANE_SMITH).orElse(null);
            User uTech3 = userRepository.findByUsername(DAVID_MILLER).orElse(null);

            if (uTech1 == null) {
                uTech1 = userRepository.save(User.builder().username(JOHN_DOE).email("john@gridpulse.com").password(passwordEncoder.encode(TEMP_PASSWORD)).fullName(NAME_JOHN_DOE).role(ROLE_TECHNICIAN).passwordChanged(false).build());
            }
            if (uTech2 == null) {
                uTech2 = userRepository.save(User.builder().username(JANE_SMITH).email("jane@gridpulse.com").password(passwordEncoder.encode(TEMP_PASSWORD)).fullName(NAME_JANE_SMITH).role(ROLE_TECHNICIAN).passwordChanged(false).build());
            }
            if (uTech3 == null) {
                uTech3 = userRepository.save(User.builder().username(DAVID_MILLER).email("david@gridpulse.com").password(passwordEncoder.encode(TEMP_PASSWORD)).fullName(NAME_DAVID_MILLER).role(ROLE_TECHNICIAN).passwordChanged(false).build());
            }

            Technician tech1 = Technician.builder()
                    .user(uTech1)
                    .employeeId("TECH-001")
                    .fullName(NAME_JOHN_DOE)
                    .phone("+91-9876543210")
                    .specialization("Transformer Maintenance, Substation Automation")
                    .availability(STATUS_AVAILABLE)
                    .experience(5)
                    .rating(4.8)
                    .currentJobs(0)
                    .currentLatitude(12.9716)
                    .currentLongitude(77.5946)
                    .status(STATUS_ACTIVE)
                    .build();

            Technician tech2 = Technician.builder()
                    .user(uTech2)
                    .employeeId("TECH-002")
                    .fullName(NAME_JANE_SMITH)
                    .phone("+91-8765432109")
                    .specialization("Cable Repair, High Voltage Breakers")
                    .availability(STATUS_AVAILABLE)
                    .experience(7)
                    .rating(4.9)
                    .currentJobs(0)
                    .currentLatitude(13.0827)
                    .currentLongitude(80.2707)
                    .status(STATUS_ACTIVE)
                    .build();

            Technician tech3 = Technician.builder()
                    .user(uTech3)
                    .employeeId("TECH-003")
                    .fullName(NAME_DAVID_MILLER)
                    .phone("+91-7654321098")
                    .specialization("Grid Protection Systems, Telecom Diagnostics")
                    .availability(STATUS_AVAILABLE)
                    .experience(4)
                    .rating(4.7)
                    .currentJobs(0)
                    .currentLatitude(17.3850)
                    .currentLongitude(78.4867)
                    .status(STATUS_ACTIVE)
                    .build();

            technicianRepository.saveAll(Arrays.asList(tech1, tech2, tech3));
            log.info("Seeded 3 technicians linked to individual accounts.");
        }


        
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
            log.info("Seeded default customers.");
        }

        
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
            log.info("Seeded repair logs for AI historical diagnostic retrieval.");
        }
    }
}
