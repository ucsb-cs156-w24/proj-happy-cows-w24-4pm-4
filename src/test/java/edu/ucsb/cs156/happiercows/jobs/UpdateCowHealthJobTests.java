package edu.ucsb.cs156.happiercows.jobs;

import edu.ucsb.cs156.happiercows.entities.Commons;
import edu.ucsb.cs156.happiercows.entities.User;
import edu.ucsb.cs156.happiercows.entities.UserCommons;
import edu.ucsb.cs156.happiercows.entities.UserCommonsKey;
import edu.ucsb.cs156.happiercows.entities.jobs.Job;
import edu.ucsb.cs156.happiercows.repositories.CommonsRepository;
import edu.ucsb.cs156.happiercows.repositories.UserCommonsRepository;
import edu.ucsb.cs156.happiercows.repositories.UserRepository;
import edu.ucsb.cs156.happiercows.services.jobs.JobContext;
import edu.ucsb.cs156.happiercows.strategies.CowHealthUpdateStrategies;
import edu.ucsb.cs156.happiercows.strategies.CowHealthUpdateStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UpdateCowHealthJobTests {
    @Mock
    CommonsRepository commonsRepository;

    @Mock
    UserCommonsRepository userCommonsRepository;

    @Mock
    UserRepository userRepository;

    private final User user = User
            .builder()
            .id(1L)
            .fullName("Chris Gaucho")
            .email("cgaucho@example.org")
            .build();

    private final Commons commons = Commons
            .builder()
            .name("test commons")
            .cowPrice(10)
            .milkPrice(2)
            .startingBalance(300)
            .startingDate(LocalDateTime.now())
            .carryingCapacity(100)
            .degradationRate(1)
            .belowCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Noop)
            .aboveCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Noop)
            .build();

    private final UserCommons userCommons = UserCommons
            .builder()
            .user(user)
.commons(commons)
            .totalWealth(300)
            .numOfCows(1)
            .cowHealth(10.0)
            .build();

    private final Job job = Job.builder().build();
    private final JobContext ctx = new JobContext(null, job);

    private void runUpdateCowHealthJob() throws Exception {
        var updateCowHealthJob = new UpdateCowHealthJob(commonsRepository, userCommonsRepository, userRepository);
        updateCowHealthJob.accept(ctx);
    }

    @Test
    void test_log_output_with_no_commons() throws Exception {
        runUpdateCowHealthJob();

        String expected = """
                Updating cow health...
                Cow health has been updated!""";
        assertEquals(expected, job.getLog());
    }

    private void setupUpdateCowHealthTestOnCommons(int totalCows) {
        when(commonsRepository.findAll()).thenReturn(List.of(commons));
        when(userCommonsRepository.findByCommonsId(commons.getId())).thenReturn(List.of(userCommons));
        when(commonsRepository.getNumCows(commons.getId())).thenReturn(Optional.of(totalCows));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void test_uses_above_capacity_update_strategy() throws Exception {
        commons.setAboveCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Constant);
        double expectedNewHealth = 9.0;

        setupUpdateCowHealthTestOnCommons(101);
        runUpdateCowHealthJob();

        assertEquals(expectedNewHealth, userCommons.getCowHealth());

        String expected = """
                Updating cow health...
                Commons test commons, degradationRate: 1.0, carryingCapacity: 100
                User: Chris Gaucho, numCows: 1, cowHealth: 10.0
                 old cow health: 10.0, new cow health: 9.0
                Cow health has been updated!""";
        assertEquals(expected, job.getLog());
    }

    @Test
    void test_uses_below_capacity_update_strategy() throws Exception {
        commons.setBelowCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Constant);
        double expectedNewHealth = 11.0;

        setupUpdateCowHealthTestOnCommons(99);
        runUpdateCowHealthJob();

        assertEquals(expectedNewHealth, userCommons.getCowHealth());

        String expected = """
                Updating cow health...
                Commons test commons, degradationRate: 1.0, carryingCapacity: 100
                User: Chris Gaucho, numCows: 1, cowHealth: 10.0
                 old cow health: 10.0, new cow health: 11.0
                Cow health has been updated!""";
        assertEquals(expected, job.getLog());
    }


    @Test
    void test_uses_below_capacity_update_strategy_if_equal_to_carrying_capacity() throws Exception {
        commons.setBelowCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Constant);
        double expectedNewHealth = 11.0;

        setupUpdateCowHealthTestOnCommons(commons.getCarryingCapacity());
        runUpdateCowHealthJob();

        assertEquals(expectedNewHealth, userCommons.getCowHealth());
        String expected = """
                Updating cow health...
                Commons test commons, degradationRate: 1.0, carryingCapacity: 100
                User: Chris Gaucho, numCows: 1, cowHealth: 10.0
                 old cow health: 10.0, new cow health: 11.0
                Cow health has been updated!""";
        assertEquals(expected, job.getLog());
    }

    @Test
    void test_cow_health_minimum_is_0() throws Exception {
        var mockStrategy = mock(CowHealthUpdateStrategy.class);
        when(mockStrategy.calculateNewCowHealth(any(), any(), anyInt())).thenReturn(-1.0);
        var newHealth = UpdateCowHealthJob.calculateNewCowHealthUsingStrategy(
                mockStrategy,
                commons,
                userCommons,
                1
        );
        assertEquals(0.0, newHealth);
    }

    @Test
    void test_cow_health_maximum_is_100() throws Exception {
        var mockStrategy = mock(CowHealthUpdateStrategy.class);
        when(mockStrategy.calculateNewCowHealth(any(), any(), anyInt())).thenReturn(101.0);
        var newHealth = UpdateCowHealthJob.calculateNewCowHealthUsingStrategy(
                mockStrategy,
                commons,
                userCommons,
                1
        );
        assertEquals(100.0, newHealth);
    }

    @Test
    void test_updating_values_for_multiple_users() throws Exception {
        var userCommons1 = userCommons;
        var userCommons2 = UserCommons
                .builder()
                .user(user)
.commons(commons)
                .totalWealth(300)
                .numOfCows(6)
                .cowHealth(20)
                .build();
        commons.setBelowCapacityHealthUpdateStrategy(CowHealthUpdateStrategies.Linear);

        when(commonsRepository.findAll()).thenReturn(List.of(commons));
        when(userCommonsRepository.findByCommonsId(commons.getId())).thenReturn(List.of(userCommons1, userCommons2));
        when(commonsRepository.getNumCows(commons.getId())).thenReturn(Optional.of(99));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        runUpdateCowHealthJob();


        String expected = """
                Updating cow health...
                Commons test commons, degradationRate: 1.0, carryingCapacity: 100
                User: Chris Gaucho, numCows: 1, cowHealth: 10.0
                 old cow health: 10.0, new cow health: 11.0
                User: Chris Gaucho, numCows: 6, cowHealth: 20.0
                 old cow health: 20.0, new cow health: 21.0
                Cow health has been updated!""";

        assertEquals(expected, job.getLog());

        assertEquals(11.0, userCommons1.getCowHealth());
        assertEquals(21.0, userCommons2.getCowHealth());
    }

    @Test
    void test_throws_exception_when_get_num_cows_fails() {
        setupUpdateCowHealthTestOnCommons(100);
        commons.setId(117);
        when(commonsRepository.getNumCows(commons.getId())).thenReturn(Optional.empty());

        var updateCowHealthJob = new UpdateCowHealthJob(commonsRepository, userCommonsRepository,
                userRepository);

        var thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            updateCowHealthJob.accept(ctx);
        });

        Assertions.assertEquals("Error calling getNumCows(117)", thrown.getMessage());
    }
}
