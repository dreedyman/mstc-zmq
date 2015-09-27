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

import org.mstc.zmq.Invoke.MethodResult;
import org.mstc.zmq.Test.Input;
import org.mstc.zmq.Test.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mstc.zmq.dispatcher.InvokerBuilder.*;

/**
 * @author Dennis Reedy
 */
public class Consumer {
    List<String> replies = new ArrayList<>();

    public void go() throws IOException {

        for (int requestNum = 0; requestNum < 10; requestNum++) {
            Input input = Input.newBuilder().setInput("Hello").build();
            byte[] response = service(Producer.class,
                                      method("go",
                                             input(
                                                      Input.class, input))).invoke();
            MethodResult result = MethodResult.parseFrom(response);
            Output output = Output.parseFrom(result.getResult());
            replies.add(String.format("[%s] Received reply %s", requestNum, output.getOutput()));
        }
        service(Producer.class, method("shutdown")).invoke();
        shutdown();
    }

    List<String> getReplies() {
        return replies;
    }
}
