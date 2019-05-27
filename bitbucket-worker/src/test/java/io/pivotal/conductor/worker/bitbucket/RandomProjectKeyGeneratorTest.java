package io.pivotal.conductor.worker.bitbucket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RandomProjectKeyGeneratorTest {

    private RandomProjectKeyGenerator projectKeyGenerator;

    @BeforeEach
    void setUp() {
        projectKeyGenerator = new RandomProjectKeyGenerator();
    }

    @Test
    void generateKey() {
        String projectKey = projectKeyGenerator.generateKey();
        assertThat(projectKey)
            .hasSize(4)
            .containsPattern(Pattern.compile("^[A-Z]{4}$"));
    }
}
