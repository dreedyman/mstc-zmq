/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mstc.zmq.dispatcher;

import org.mstc.zmq.lookup.LookupService;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Reedy
 */
public class TestRunner {
    static LookupService lookupService;

    @BeforeClass
    public static void setup() {
        lookupService = new LookupService();
    }

    //@After
    public void teardown() {
        lookupService.terminate();
    }

    @Test
    public void testInvokeStartProducerFirst() throws InterruptedException, IOException {
        int numRequests = 1;
        Consumer consumer = new Consumer();
        Thread consumerThread = new Thread(() -> {
            try {
                consumer.go(numRequests);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("Create Dispatcher/Producer");
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.register(new Producer(), getAttributes());
        //dispatcher.register(new Producer(), attributes);
        System.out.println("Start Consumer");
        consumerThread.start();
        consumerThread.join();
        Assert.assertTrue(consumer.getReplies().size()==numRequests);
        consumer.getReplies().forEach(System.out::println);
        dispatcher.shutdown();
    }

    @Test
    public void testInvokeStartConsumerFirst() throws InterruptedException, IOException {
        int numRequests = 1;
        Consumer consumer = new Consumer();
        Thread consumerThread = new Thread(() -> {
            try {
                consumer.go(numRequests);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("Start Consumer");
        consumerThread.start();

        System.out.println("Create Dispatcher/Producer");
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.register(new Producer(), getAttributes());
        //dispatcher.register(new Producer(), attributes);
        consumerThread.join();

        Assert.assertTrue(consumer.getReplies().size()==numRequests);
        consumer.getReplies().forEach(System.out::println);
    }

    Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("name", "Producer");
        attributes.put("interface", Producer.class.getName());
        attributes.put("description", "Test thingy");
        return attributes;
    }
}
