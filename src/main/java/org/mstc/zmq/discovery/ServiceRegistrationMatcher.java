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
package org.mstc.zmq.discovery;

import org.mstc.zmq.json.discovery.ServiceRegistration;
import org.mstc.zmq.json.discovery.ServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Dennis Reedy
 */
public class ServiceRegistrationMatcher {
    private static Logger logger = LoggerFactory.getLogger(ServiceRegistrationMatcher.class);

    public boolean match(ServiceTemplate template, ServiceRegistration serviceRegistration) {
        if(template.getAllFields().size()==0) {
            return true;
        }
        boolean matched = true;
        /*for(Map.Entry<Descriptors.FieldDescriptor, Object> entry :template.getAllFields().entrySet()) {
            String fieldName = entry.getKey().getName();*/
        for(Map.Entry<String, String> entry : template.getAllFields().entrySet()) {
            String fieldName = entry.getKey();
            String prefix = fieldName.substring(0,1).toUpperCase();
            String suffix = fieldName.substring(1, fieldName.length());
            String getter = String.format("get%s%s", prefix, suffix);
            try {
                Method getterMethod = serviceRegistration.getClass().getMethod(getter);
                Object value = getterMethod.invoke(serviceRegistration);
                if(value!=null) {
                    if(!value.equals(entry.getValue())) {
                        matched = false;
                        break;
                    }
                } else {
                    matched = false;
                    break;
                }
            } catch (IllegalAccessException  | NoSuchMethodException | InvocationTargetException e) {
                logger.error("Failed trying to match {} to {}", template, serviceRegistration, e);
                matched = false;
                break;
            }
        }
        return matched;
    }
}
