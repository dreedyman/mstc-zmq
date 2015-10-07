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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dennis Reedy
 */
public class ServiceRegistration {
    private String name;
    private String groupName;
    private String endPoint;
    private String architecture;
    private String description;
    private String interfaceName;
    private String language;
    private List<String> methodNames = new ArrayList<>();

    public String getEndPoint() {
        return endPoint;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public String getName() {
        return name;
    }

    public byte[] toByteArray() throws IOException {
        return toJSON().getBytes();
    }

    public static ServiceRegistration parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, ServiceRegistration.class.getDeclaredFields(), b);
        return b.build();
    }

    public String toJSON() throws IOException {
        return Encoder.encode(this);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        ServiceRegistration serviceRegistration = new ServiceRegistration();
        public Builder setName(String name) {
            serviceRegistration.name = name;
            return this;
        }
        public Builder setGroupName(String group) {
            serviceRegistration.groupName = group;
            return this;
        }
        public Builder setEndPoint(String endPoint) {
            serviceRegistration.endPoint = endPoint;
            return this;
        }
        public Builder setArchitecture(String architecture) {
            serviceRegistration.architecture = architecture;
            return this;
        }
        public Builder setDescription(String description) {
            serviceRegistration.description = description;
            return this;
        }
        public Builder setInterfaceName(String interfaceName) {
            serviceRegistration.interfaceName = interfaceName;
            return this;
        }
        public Builder setLanguage(String language) {
            serviceRegistration.language = language;
            return this;
        }
        public Builder addMethodName(String methodName) {
            serviceRegistration.methodNames.add(methodName);
            return this;
        }
        public Builder addMethodNames(Collection<String > methodNames) {
            serviceRegistration.methodNames.addAll(methodNames);
            return this;
        }

        public ServiceRegistration build() {
            return serviceRegistration;
        }

    }
}
