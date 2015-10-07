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

import org.mstc.zmq.annotations.HandlerNotify;
import org.mstc.zmq.annotations.Remoteable;
import org.mstc.zmq.json.test.*;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;

/**
 * @author Dennis Reedy
 */
public class Producer {
    private Handler handler;

    @HandlerNotify
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Remoteable
    public Output go(Input input) {
        System.out.println("Received request: "+ input.getInput());
        double[] doubles = input.getDoubles();
        double[] squared = new double[doubles.length];
        int[] ints = input.getInts();
        int[] halved = new int[ints.length];

        for(int i=0; i<squared.length; i++) {
            squared[i] = Math.pow(doubles[i], 2);
            System.out.println(new BigDecimal(doubles[i]).toPlainString()+" => "+new BigDecimal(squared[i]).toPlainString());
        }

        for(int i=0; i<ints.length; i++) {
            halved[i] = ints[i]/2;
            System.out.println(new BigDecimal(ints[i]).toPlainString()+" => "+new BigDecimal(halved[i]).toPlainString());
        }
        return Output.newBuilder().setOutput(String.format("%s World", input.getInput()))
                   .setDoubles(squared)
                   .setInts(halved).build();
    }

    @PreDestroy
    public void shutdown() {
        handler.deregister(this);
        System.out.println("Producer shutdown");
    }
}
