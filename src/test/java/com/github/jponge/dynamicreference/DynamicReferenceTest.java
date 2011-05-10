package com.github.jponge.dynamicreference;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DynamicReferenceTest {

    @Test
    public void single_thread() {

        DynamicReference<String> reference = new DynamicReference<String>("abcd");
        Operation<Integer, String> countCharsOperation = new Operation<Integer, String>() {
            @Override
            public Integer apply(String reference) {
                return reference.length();
            }
        };

        assertThat(reference.perform(countCharsOperation), is(4));

        reference.discard();
        try {
            reference.perform(countCharsOperation);
            throw new RuntimeException("An exception should have been thrown");
        } catch (IllegalStateException ignored) {

        }

        reference.set("123");
        assertThat(reference.perform(countCharsOperation), is(3));

        reference.discard();
        assertThat(reference.perform(countCharsOperation, 666), is(666));
    }

    @Test
    public void multi_thread() throws InterruptedException {

        final DynamicReference<String> reference = new DynamicReference<String>("abcd");
        final Operation<Integer, String> countCharsOperation = new Operation<Integer, String>() {
            @Override
            public Integer apply(String reference) {
                return reference.length();
            }
        };

        Callable<Void> operationInvoker = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (int i = 0; i < 100000; i++) {
                    try {
                        assertThat(reference.perform(countCharsOperation), is(4));
                    } catch (IllegalStateException ignored) {
                    }
                }
                return null;
            }
        };

        Callable<Void> referenceBreaker = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Random rand = new Random();
                for (int i = 0; i < 100000; i++) {
                    switch (rand.nextInt(2)) {
                        case 0:
                            reference.discard();
                            break;
                        case 1:
                            reference.set("abcd");
                            break;
                        default:
                            break;
                    }
                }
                return null;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.invokeAll(asList(operationInvoker, referenceBreaker));
    }
}
