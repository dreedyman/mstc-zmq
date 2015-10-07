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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dennis Reedy
 */
public class LookupResult {
    private Status status;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public List<ServiceRegistration> getServiceRegistrationList() {
        return serviceRegistrations;
    }

    public Status getStatus() {
        return status;
    }

    public static LookupResult parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, LookupResult.class.getDeclaredFields(), b);
        return b.build();
    }

    public String toJSON() throws IOException {
        return Encoder.encode(this);
    }

    public byte[] toByteArray() throws IOException {
        return toJSON().getBytes();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        LookupResult lookupResult = new LookupResult();

        public Builder setStatus(Status status) {
            lookupResult.status = status;
            return this;
        }

        public Builder addServiceRegistration(ServiceRegistration serviceRegistration) {
            lookupResult.serviceRegistrations.add(serviceRegistration);
            return this;
        }

        public Builder addServiceRegistrations(Collection<ServiceRegistration> list) {
            lookupResult.serviceRegistrations.addAll(list);
            return this;
        }

        public LookupResult build() {
            return lookupResult;
        }
    }
}
