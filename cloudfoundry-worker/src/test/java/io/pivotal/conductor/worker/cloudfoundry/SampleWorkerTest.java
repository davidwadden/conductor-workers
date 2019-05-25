package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SampleWorkerTest {

    @Test
    void test() {
        SampleWorker sampleWorker = new SampleWorker();
        assertThat(sampleWorker).isNotNull();
    }

}
