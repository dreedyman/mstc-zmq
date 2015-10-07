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
public class MethodResult {
    private Status status;
    private String type;
    private String result;

    public String getResult() {
        return result;
    }

    public Status getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static MethodResult parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, MethodResult.class.getDeclaredFields(), b);
        return b.build();
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static class Builder {
        MethodResult result = new MethodResult();

        public Builder setStatus(Status status) {
            result.status = status;
            return this;
        }

        public Builder setType(String type) {
            result.type = type;
            return this;
        }

        public Builder setResult(String result) {
            this.result.result = result;
            return this;
        }

        public MethodResult build() {
            return result;
        }
    }
}
