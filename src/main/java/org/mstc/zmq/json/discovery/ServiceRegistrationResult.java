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
package org.mstc.zmq.json.discovery;

import org.mstc.zmq.json.Decoder;
import org.mstc.zmq.json.Encoder;
import org.mstc.zmq.json.invoke.Status;

import java.io.IOException;

/**
 * @author Dennis Reedy
 */
public class ServiceRegistrationResult {
    private Status status;
    private String uuid;

    public Status getStatus() {
        return status;
    }

    public String getUuid() {
        return uuid;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static ServiceRegistrationResult parseFrom(byte[] input) throws IOException {
        System.out.println("[1] "+new String(input));
        Builder b = new Builder();
        Decoder.decode(input, ServiceRegistrationResult.class.getDeclaredFields(), b);
        return b.build();
    }

    public static class Builder {
        ServiceRegistrationResult result = new ServiceRegistrationResult();

        public Builder setStatus(Status status) {
            result.status = status;
            return this;
        }

        public Builder setUuid(String uuid) {
            result.uuid = uuid;
            return this;
        }

        public ServiceRegistrationResult build() {
            return result;
        }

    }
}
