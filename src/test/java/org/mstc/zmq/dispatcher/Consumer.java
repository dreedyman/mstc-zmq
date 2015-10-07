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

/*import org.mstc.zmq.proto.Test.Input;
import org.mstc.zmq.proto.Test.Output;*/

import org.mstc.zmq.json.invoke.MethodResult;
import org.mstc.zmq.json.test.Input;
import org.mstc.zmq.json.test.Output;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.mstc.zmq.dispatcher.InvokerBuilder.*;

/**
 * @author Dennis Reedy
 */
public class Consumer {
    List<String> replies = new ArrayList<>();

    public void go(int numRequests) throws IOException {
        Random rand = new Random();

        for (int requestNum = 0; requestNum < numRequests; requestNum++) {
            double[] doubles = createDoubles(rand.nextInt(), 4);
            int[] ints = createInts(rand.nextInt(), 3);
            System.out.println("doubles");
            for(int i=0; i< doubles.length; i++)
                System.out.println("\t"+new BigDecimal(doubles[i]).toPlainString());
            System.out.println("ints");
            for(int i=0; i< ints.length; i++)
                System.out.println("\t" + new BigDecimal(ints[i]).toPlainString());

            Input input = Input.newBuilder().setInput("Hello").setDoubles(doubles).setInts(ints).build();

            MethodResult result = service(Producer.class,
                                          method("go",
                                                 input(Input.class, input))).invoke();

            Output output = Output.parseFrom(result.getResult());
            replies.add(String.format("[%s] Received reply %s", requestNum, output.getOutput()));
            double[] squared = new double[doubles.length];
            for(int i=0; i<squared.length; i++) {
                squared[i] = Math.pow(doubles[i], 2);
            }

            int[] halved = new int[ints.length];
            for(int i=0; i<ints.length; i++) {
                halved[i] = ints[i]/2;
            }

            for(int i=0; i< squared.length; i++) {
                double d = squared[i];
                double d1 = output.getDoubles()[i];
                System.out.println(d+" = "+d1);
            }

            if(!Arrays.equals(squared, output.getDoubles())) {
                throw new IOException("double arrays do not match");
            } else {
                System.out.println("Matched double arrays!");
            }

            if(!Arrays.equals(halved, output.getInts())) {
                throw new IOException("halved arrays do not match");
            } else {
                System.out.println("Matched int arrays!");
            }
        }
        service(Producer.class, method("shutdown")).invoke();
        shutdown();
    }

    int[] createInts(int start, int length) {
        int[] ints = new int[length];
        for(int i=0; i < length; i++) {
            ints[i] = i+start;
        }
        return ints;
    }

    double[] createDoubles(int start, int length) {
        double[] ds = new double[length];
        for(int d=0; d < length; d++) {
            ds[d] = (double) d+start;
        }
        return ds;
    }

    List<String> getReplies() {
        return replies;
    }
}
