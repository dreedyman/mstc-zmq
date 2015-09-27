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
package org.mstc.zmq.dispatcher;

import org.mstc.zmq.Discovery.ServiceRegistration;
import org.mstc.zmq.Discovery.ServiceTemplate;
import org.mstc.zmq.Invoke.MethodRequest;
import org.mstc.zmq.discovery.DiscoveryClient;
import org.mstc.zmq.lookup.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Stack;

/**
 * @author Dennis Reedy
 */
public class Invoker {
    private Stack<InvokeRequest> stack = new Stack<>();
    private ZContext context;
    private ZMQ.Socket client;
    private DiscoveryClient discoveryClient;
    private static Logger logger = LoggerFactory.getLogger(Invoker.class);

    public Invoker() {
        context = new ZContext(1);
    }

    class InvokeRequest {
        ServiceTemplate template;
        MethodRequest methodRequest;

        public InvokeRequest(MethodRequest methodRequest, ServiceTemplate template) {
            this.methodRequest = methodRequest;
            this.template = template;
        }
    }

    public void push(MethodRequest methodRequest, ServiceTemplate template) {
        stack.push(new InvokeRequest(methodRequest, template));
    }

    public byte[] invoke() throws InvokerException {
        InvokeRequest request = stack.pop();
        if(discoveryClient==null) {
            discoveryClient = new DiscoveryClient(context);
            discoveryClient.discover(System.getProperty("mstc.zmq.lookup"));
        }
        List<ServiceRegistration> services = discoveryClient.getServiceCache(request.template);
        int retries = 0;
        while(services.isEmpty() && retries < 10) {
            System.out.println("Need to lookup!!!!");
            try {
                services = discoveryClient.lookup(request.template);
            } catch (LookupException e) {
                throw new InvokerException("Error invoking lookup service", e);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("Somebody interrupted invoke", e);
                break;
            }
            retries++;
        }
        if(services.isEmpty())
            throw new InvokerException("No services");

        client = context.createSocket(ZMQ.REQ);
        client.connect(services.get(0).getEndPoint());
        client.send(request.methodRequest.toByteArray());
        return client.recv();
    }

    public void shutdown() {
        context.destroySocket(client);
        discoveryClient.terminate();
        context.destroy();
    }
}
