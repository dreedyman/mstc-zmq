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
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @author Dennis Reedy
 */
public class ServiceRegistrationClient {
    ZContext context;
    ZMQ.Socket socket;

    public ServiceRegistrationClient() {
        this(new ZContext(1));
    }

    public ServiceRegistrationClient(ZContext context) {
        this.context = context;
        socket = context.createSocket(ZMQ.REQ);
    }

    public ServiceRegistrationResult register(ServiceRegistration serviceRegistration,
                                              String lookupServiceAddress) throws InvalidProtocolBufferException {
        socket.connect("tcp://"+lookupServiceAddress+":"+DiscoveryConstants.SERVICE_REGISTRATION);
        socket.send(serviceRegistration.toByteArray());
        byte[] response = socket.recv();
        return ServiceRegistrationResult.parseFrom(response);
    }
}
