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

import org.mstc.zmq.Invoke.MethodRequest;
import org.mstc.zmq.Invoke.MethodResult;
import org.mstc.zmq.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Reedy
 */
public class ConsumerUsingZMQApi {

    List<String> replies = new ArrayList<>();

    public void go() throws IOException {
        ZContext context = new ZContext(1);
        ZMQ.Socket requester = context.createSocket(ZMQ.REQ);
        requester.connect("tcp://localhost:5559");

        for (int requestNum = 0; requestNum < 10; requestNum++) {
            Test.Input input = Test.Input.newBuilder().setInput("Hello").build();
            MethodRequest methodRequest = MethodRequest.newBuilder()
                                              .setMethod("go")
                                              .setInput(input.toByteString()).build();
            requester.send(methodRequest.toByteArray());
            byte[] response = requester.recv();
            MethodResult result = MethodResult.parseFrom(response);
            Test.Output output = Test.Output.parseFrom(result.getResult());
            replies.add(String.format("[%s] Received reply %s", requestNum, output.getOutput()));
        }

        MethodRequest methodRequest = MethodRequest.newBuilder().setMethod("shutdown").build();
        requester.send(methodRequest.toByteArray());
        context.destroy();

    }

    List<String> getReplies() {
        return replies;
    }
}

