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
import org.mstc.zmq.Test;

import javax.annotation.PreDestroy;

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
    public Test.Output go(Test.Input input) {
        System.out.println("Received request: "+ input.getInput());
        return Test.Output.newBuilder().setOutput(String.format("%s World", input.getInput())).build();
    }

    @PreDestroy
    public void shutdown() {
        handler.deregister(this);
        System.out.println("Producer shutdown");
    }
}
