package io.pivotal.conductor.worker.bitbucket;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomProjectKeyGenerator implements ProjectKeyGenerator {

    private static final int PROJECT_KEY_LENGTH = 4;

    @Override
    public String generateKey() {

        return RandomStringUtils
            .random(PROJECT_KEY_LENGTH, true, false)
            .toUpperCase();
    }

}
