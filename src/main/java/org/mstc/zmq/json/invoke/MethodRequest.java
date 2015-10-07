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
package org.mstc.zmq.json.invoke;

import org.mstc.zmq.json.Decoder;
import org.mstc.zmq.json.Encoder;

import java.io.IOException;

/**
 * @author Dennis Reedy
 */
public class MethodRequest {
    private String method;
    private String type;
    private String input;

    public String getInput() {
        return input;
    }

    public String getMethod() {
        return method;
    }

    public String getType() {
        return type;
    }

    public static MethodRequest parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, MethodRequest.class.getDeclaredFields(), b);
        return b.build();
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        MethodRequest request = new MethodRequest();

        public Builder setMethod(String method) {
            request.method = method;
            return this;
        }

        public Builder setType(String type) {
            request.type = type;
            return this;
        }

        public Builder setInput(String input) {
            request.input = input;
            return this;
        }

        public MethodRequest build() {
            return request;
        }
    }
}
