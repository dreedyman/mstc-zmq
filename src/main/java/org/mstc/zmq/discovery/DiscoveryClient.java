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
import org.mstc.zmq.Discovery.LookupResult;
import org.mstc.zmq.Discovery.ServiceRegistration;
import org.mstc.zmq.Discovery.ServiceTemplate;
import org.mstc.zmq.Invoke;
import org.mstc.zmq.lookup.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Dennis Reedy
 */
public class DiscoveryClient {
    private final ZContext context;
    private ZMQ.Socket lookup;
    private final List<String> groupNames = new ArrayList<>();
    private String lookupServiceAddress ;
    private final AtomicBoolean keepAlive = new AtomicBoolean(true);
    private final List<ServiceRegistration> serviceCache = new ArrayList<>();
    private boolean closeContextOnShutdown = true;
    private final ExecutorService discoPool = Executors.newSingleThreadExecutor();
    private List<ServiceDiscoveryListener> serviceDiscoveryListeners = new ArrayList<>();
    private Set<ServiceRegistrationListener> serviceRegistrationListener = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryClient.class);

    public DiscoveryClient() {
        context = new ZContext(1);
    }

    public DiscoveryClient(ZContext context) {
        this.context = context;
        closeContextOnShutdown = false;
    }

    public DiscoveryClient setGroups(String... groups) {
        Collections.addAll(groupNames, groups);
        return this;
    }

    public void addServiceRegistrationListener(ServiceRegistrationListener listener) {
        serviceRegistrationListener.add(listener);
    }

    public void removeServiceRegistrationListener(ServiceRegistrationListener listener) {
        serviceRegistrationListener.remove(listener);
    }

    public DiscoveryClient discover(String lookupServiceAddress ) {
        this.lookupServiceAddress  = lookupServiceAddress;
        boolean matched = false;
        for(ServiceDiscoveryListener l : serviceDiscoveryListeners) {
            if(l.getLookupServiceAddress().equals(lookupServiceAddress)) {
                matched = true;
                break;
            }
        }
        if(!matched) {
            ServiceDiscoveryListener serviceDiscoveryListener = new ServiceDiscoveryListener(lookupServiceAddress);
            serviceDiscoveryListeners.add(serviceDiscoveryListener);
            discoPool.submit(serviceDiscoveryListener);
        }
        return this;
    }

    public List<ServiceRegistration> getServiceCache() {
        List<ServiceRegistration> cache = new ArrayList<>();
        cache.addAll(serviceCache);
        return cache;
    }

    public List<ServiceRegistration> getServiceCache(ServiceTemplate filter) {
        ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
        return serviceCache.stream()
                   .filter(registration -> matcher.match(filter, registration))
                   .collect(Collectors.toList());
    }

    public List<ServiceRegistration> lookup(ServiceTemplate serviceTemplate) throws LookupException {
        if(lookup==null) {
            lookup = context.createSocket(ZMQ.REQ);
        }
        lookup.connect("tcp://" + lookupServiceAddress + ":" + DiscoveryConstants.SERVICE_LOOKUP);
        lookup.send(serviceTemplate.toByteArray(), 0);
        byte[] response = lookup.recv(0);
        try {
            LookupResult result = LookupResult.parseFrom(response);
            if(result.getStatus().getResult()!= Invoke.Result.OKAY) {
                throw new LookupException("Failed response from LookupService: "+result.getStatus().getResult());
            }
            List<ServiceRegistration> services = result.getServiceRegistrationList();
            serviceCache.addAll(services);
            return services;
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error receiving LookupResponse", e);
            throw new LookupException("Error receiving LookupResponse", e);
        }
    }

    public void terminate() {
        keepAlive.set(false);
        if(lookup!=null)
            lookup.close();
        serviceDiscoveryListeners.forEach(ServiceDiscoveryListener::terminate);
        if(closeContextOnShutdown)
            context.destroy();
        discoPool.shutdown();
    }

    class ServiceDiscoveryListener implements Runnable {
        String lookupServiceAddress;
        ZMQ.Socket subscriber;

        public ServiceDiscoveryListener(String lookupServiceAddress ) {
            this.lookupServiceAddress  = lookupServiceAddress ;
        }

        String getLookupServiceAddress() {
            return lookupServiceAddress;
        }

        void terminate() {
            context.destroySocket(subscriber);
        }

        @Override public void run() {
            subscriber = context.createSocket(ZMQ.SUB);
            subscriber.connect("tcp://" + lookupServiceAddress  + ":" + DiscoveryConstants.SERVICE_REGISTRATION_PUB);
            for(String group : groupNames) {
                System.out.println("Subscribe to "+group);
                subscriber.subscribe(group.getBytes());
            }
            while(keepAlive.get()) {
                String group = subscriber.recvStr(0);
                byte[] contents = subscriber.recv(0);
                System.out.println("Received group: "+group);
                try {
                    ServiceRegistration serviceRegistration = ServiceRegistration.parseFrom(contents);
                    serviceCache.add(serviceRegistration);
                    //if(logger.isDebugEnabled()) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("\n");
                        builder.append("group: ").append(group).append("\n");
                        builder.append("endpoint: ").append(serviceRegistration.getEndPoint()).append("\n");
                        builder.append("name: ").append(serviceRegistration.getName());
                        logger.debug(builder.toString());
                    //}
                } catch (InvalidProtocolBufferException e) {
                    logger.warn("Failed getting ServiceRegistration notification", e);
                }
            }
            logger.info("shutting down...");
            context.destroySocket(subscriber);
        }
    }
}
