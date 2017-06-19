package com.amihaiemil.charles.github;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * Unit tests for {@link IndexExistsCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class IndexExistsCheckTestCase {

    /**
     * IndexExistsCheck can tell if an index.
     */
    @Test
    public void indexExists() {
        IndexExistsCheck iec = new IndexExistsCheck(
            new AwsEsRepository.Fake(true),
            new Step.Fake(true), new Step.Fake(false)
        );
        iec.perform(Mockito.mock(Command.class), Mockito.mock(Logger.class));
    }

    /**
     * IndexExistsCheck can tell if an index does not exist.
     */
    @Test
    public void indexDoesntExist() {
        IndexExistsCheck iec = new IndexExistsCheck(
            new AwsEsRepository.Fake(false),
            new Step.Fake(false), new Step.Fake(true)
        );
        iec.perform(Mockito.mock(Command.class), Mockito.mock(Logger.class));
    }
}
