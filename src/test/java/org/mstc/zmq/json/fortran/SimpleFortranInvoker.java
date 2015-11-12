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
package org.mstc.zmq.json.fortran;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Dennis Reedy
 */
public class SimpleFortranInvoker {

    @Test
    public void test() throws IOException {
        ZContext context = new ZContext(1);
        ZMQ.Socket requester = context.createSocket(ZMQ.REQ);
        requester.connect("tcp://localhost:5555");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        requester.send("isprime", ZMQ.SNDMORE);
        requester.send(mapper.writeValueAsString(new Prime(7919)));
        Prime prime = mapper.readValue(requester.recv(), Prime.class);
        assertTrue(prime.getResult());

        requester.send("average", ZMQ.SNDMORE);
        requester.send(mapper.writeValueAsString(new Average(1.2, 3.4, 5.6, 7.8)));

        Average average = mapper.readValue(requester.recv(), Average.class);
        System.out.printf("%.2f\n", average.getResult());
        assertTrue(4.5==average.getResult());

        requester.close();
        context.destroy();
    }

    public static class Prime {
        private double value;
        private String result;

        public Prime() {
        }

        public Prime(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public boolean getResult() {
            return Boolean.parseBoolean(result);
        }
    }

    public static class Average {
        private List<Double> values = new ArrayList<>();
        private Double result;

        public Average() {
        }

        public Average(Double... args) {
            Collections.addAll(values, args);
        }

        public List<Double> getValues() {
            return values;
        }

        public void setResult(Double result) {
            this.result = result;
        }

        public Double getResult() {
            return result;
        }

    }
}
