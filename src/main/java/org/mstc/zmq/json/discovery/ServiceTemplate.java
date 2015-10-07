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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Reedy
 */
public class ServiceTemplate {
    private String name;
    private String interfaceName;
    private String groupName;
    private String description;
    private String language;
    private String architecture;
    //private static Logger logger = LoggerFactory.getLogger(ServiceTemplate.class);

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAllFields() {
        Map<String, String> fieldMap = new HashMap<>();
        for(Field f : ServiceTemplate.class.getDeclaredFields()) {
            try {
                Object value = f.get(this);
                if(value!=null) {
                    fieldMap.put(f.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                ;
            }
        }
        return fieldMap;
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static ServiceTemplate parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, ServiceTemplate.class.getDeclaredFields(), b);
        return b.build();
    }

    public static ServiceTemplate getDefaultInstance() {
        return new ServiceTemplate();
    }

    public static class Builder {
        ServiceTemplate template = getDefaultInstance();

        public Builder setName(String name) {
            template.name = name;
            return this;
        }

        public Builder setInterfaceName(String interfaceName) {
            template.interfaceName = interfaceName;
            return this;
        }

        public Builder setGroupName(String groupName) {
            template.groupName = groupName;
            return this;
        }

        public Builder setDescription(String description) {
            template.description = description;
            return this;
        }

        public Builder setLanguage(String language) {
            template.language = language;
            return this;
        }

        public Builder setArchitecture(String architecture) {
            template.architecture = architecture;
            return this;
        }

        public ServiceTemplate build() {
            return template;
        }
    }
}
