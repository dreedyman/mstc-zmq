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

import com.google.protobuf.InvalidProtocolBufferException;
import org.mstc.zmq.Discovery.ServiceRegistration;
import org.mstc.zmq.Discovery.ServiceRegistrationResult;
import org.mstc.zmq.Discovery.ServiceTemplate;
import org.mstc.zmq.lookup.LookupException;
import org.mstc.zmq.lookup.LookupService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LookupServiceTest {
    String address;
    LookupService lookupService;
    DiscoveryClient discoveryClient;

    @Before
    public void setup() throws UnknownHostException {
        address = InetAddress.getLocalHost().getHostAddress();
        lookupService = new LookupService();
        discoveryClient = new DiscoveryClient();
    }

    @After
    public void cleanup() {
        lookupService.terminate();
        discoveryClient.terminate();
    }

    @Test
    public void testDiscovery() throws UnknownHostException, InvalidProtocolBufferException, InterruptedException, LookupException {
        ServiceRegistration serviceRegistration = ServiceRegistration.newBuilder()
                                                      .setName("Grand Poobah")
                                                      .setGroupName("test")
                                                      .setEndPoint("tcp://" + address + ":1234")
                                                      .addMethodName("foo")
                                                      .addMethodName("bar").build();

        ServiceRegistrationClient registrationClient = new ServiceRegistrationClient();
        ServiceRegistrationResult result = registrationClient.register(serviceRegistration, address);
        System.out.println("Status: ["+result.getStatus().getResult()+"]");
        System.out.println("UUID: "+result.getUuid());

        discoveryClient.setGroups("test").discover(address);
        discoveryClient.lookup(ServiceTemplate.getDefaultInstance());
        int retryCount = 0;
        while(discoveryClient.getServiceCache().isEmpty() && retryCount<30) {
            System.out.println("Waiting for service cache notification...");
            Thread.sleep(100);
            retryCount++;
        }
        Assert.assertFalse(discoveryClient.getServiceCache().isEmpty());
        for(ServiceRegistration service : discoveryClient.getServiceCache()) {
            StringBuilder builder = new StringBuilder();
            builder.append("group: ").append(service.getGroupName()).append("\n");
            builder.append("endpoint: ").append(serviceRegistration.getEndPoint()).append("\n");
            builder.append("name: ").append(serviceRegistration.getName());
            System.out.println(builder.toString());
        }
    }
}