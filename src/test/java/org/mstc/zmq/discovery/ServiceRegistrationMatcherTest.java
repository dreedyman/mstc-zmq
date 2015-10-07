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

/*import org.mstc.zmq.proto.Discovery.ServiceRegistration;
import org.mstc.zmq.proto.Discovery.ServiceTemplate;*/

import org.mstc.zmq.json.discovery.ServiceRegistration;
import org.mstc.zmq.json.discovery.ServiceTemplate;
import org.junit.Assert;
import org.junit.Test;

public class ServiceRegistrationMatcherTest {
    @Test
    public void testMatch() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                                                                .setName("Grand Poobah")
                                                                .setGroupName("test")
                                                                .setEndPoint("tcp://1.1.1.1:1234")
                                                                .addMethodName("foo")
                                                                .addMethodName("bar").build();

        ServiceTemplate template = ServiceTemplate.getDefaultInstance();
        Assert.assertTrue(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setDescription("foo").build();
        Assert.assertFalse(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setName("Grand Poobah").build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setName("Grand Poobah").setGroupName("foo").build();
        Assert.assertFalse(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setName("Grand Poobah").setGroupName("test").build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setGroupName("foo").build();
        Assert.assertFalse(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setGroupName("test").build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));

    }

    @Test
    public void testMatchWithDescription() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                                                      .setName("Grand Poobah")
                                                      .setGroupName("test")
                                                      .setEndPoint("tcp://1.1.1.1:1234")
                                                      .setDescription("foo").build();

        ServiceTemplate template = ServiceTemplate.getDefaultInstance();
        Assert.assertTrue(matcher.match(template, serviceRegistration));

        template = ServiceTemplate.newBuilder().setDescription("foo").build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));
    }

    @Test
    public void testMatchAll() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                                                      .setName("Grand Poobah")
                                                      .setGroupName("test")
                                                      .setEndPoint("tcp://1.1.1.1:1234")
                                                      .setInterfaceName(ServiceRegistrationMatcher.class.getName())
                                                      .setDescription("foo").build();

        ServiceTemplate template = ServiceTemplate.newBuilder()
                                       .setName(serviceRegistration.getName())
                                       .setGroupName(serviceRegistration.getGroupName())
                                       .setDescription(serviceRegistration.getDescription())
                                       .setInterfaceName(serviceRegistration.getInterfaceName()).build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));
    }

    @Test
    public void testMatchInterfaceAndGroup() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                                                      .setName("Grand Poobah")
                                                      .setGroupName("test")
                                                      .setEndPoint("tcp://1.1.1.1:1234")
                                                      .setInterfaceName(ServiceRegistrationMatcher.class.getName())
                                                      .setDescription("foo").build();

        ServiceTemplate template = ServiceTemplate.newBuilder()
                                       .setGroupName(serviceRegistration.getGroupName())
                                       .setInterfaceName(serviceRegistration.getInterfaceName()).build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));
    }

    @Test
    public void testArchitectureMatch() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                .setName("Grand Poobah")
                .setGroupName("test")
                .setEndPoint("tcp://1.1.1.1:1234")
                .setInterfaceName(ServiceRegistrationMatcher.class.getName())
                .setArchitecture(System.getProperty("os.arch"))
                .setLanguage("Java").build();

        ServiceTemplate template = ServiceTemplate.newBuilder()
                .setGroupName(serviceRegistration.getGroupName())
                .setArchitecture(serviceRegistration.getArchitecture()).build();
        Assert.assertTrue(matcher.match(template, serviceRegistration));
    }

    @Test
    public void testArchitectureNoMatch() {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                .setName("Grand Poobah")
                .setGroupName("test")
                .setEndPoint("tcp://1.1.1.1:1234")
                .setInterfaceName(ServiceRegistrationMatcher.class.getName())
                .setArchitecture(System.getProperty("os.arch"))
                .setLanguage("Java").build();

        ServiceTemplate template = ServiceTemplate.newBuilder()
                .setGroupName(serviceRegistration.getGroupName())
                .setArchitecture("x86").build();
        Assert.assertFalse(matcher.match(template, serviceRegistration));
    }

}