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
package org.mstc.zmq.lookup;

import org.mstc.zmq.Discovery.LookupResult;
import org.mstc.zmq.Discovery.ServiceRegistration;
import org.mstc.zmq.Discovery.ServiceRegistrationResult;
import org.mstc.zmq.Discovery.ServiceTemplate;
import org.mstc.zmq.Invoke.Result;
import org.mstc.zmq.Invoke.Status;
import org.mstc.zmq.discovery.DiscoveryConstants;
import org.mstc.zmq.discovery.ServiceRegistrationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Dennis Reedy
 */
public class LookupService {
    private final ZContext context;
    private final Map<UUID, ServiceRegistration> registrations = new ConcurrentHashMap<>();
    private final ExecutorService registrationPool = Executors.newCachedThreadPool();
    private final static Logger logger = LoggerFactory.getLogger(LookupService.class);
    private static final BlockingQueue<ServiceRegistration> toPublish = new LinkedBlockingQueue<>();
    private final AtomicBoolean keepAlive = new AtomicBoolean(true);
    private final List<RunnableClosable> list = new ArrayList<>();

    public LookupService() {
        this(new ZContext(1));
    }

    public LookupService(ZContext context) {
        this.context = context;
        list.add(new RegistryPublisher());
        list.add(new RegistryListener());
        list.add(new ServiceLookup());
        list.forEach(registrationPool::execute);
    }

    interface RunnableClosable extends Runnable {
        void close();
    }

    public void terminate() {
        keepAlive.set(false);
        list.forEach(LookupService.RunnableClosable::close);
        context.destroy();
        registrationPool.shutdownNow();
    }

    private Status status(Result result) {
        return status(result, null);
    }

    private Status status(Result result, String message) {
        if(message==null)
            return Status.newBuilder().setResult(result).build();
        return Status.newBuilder().setResult(result).setStatus(message).build();
    }

    private ServiceRegistrationResult result(Status status) {
        return result(status, null);
    }

    private ServiceRegistrationResult result(Status status, String uuid) {
        if (uuid ==null)
            return ServiceRegistrationResult.newBuilder().setStatus(status).build();
        return ServiceRegistrationResult.newBuilder().setStatus(status).setUuid(uuid).build();
    }

    class ServiceRegistrationAction {
        ServiceRegistration serviceRegistration;
        String action;
    }

    class RegistryListener implements RunnableClosable {
        ZMQ.Socket socket;

        @Override public void run() {
            socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:" + DiscoveryConstants.SERVICE_REGISTRATION);
            while (!Thread.currentThread().isInterrupted()) {
                byte[] recv = socket.recv(0);
                ServiceRegistration registration;
                try {
                    registration = ServiceRegistration.parseFrom(recv);
                    UUID uuid = UUID.randomUUID();
                    registrations.put(uuid, registration);
                    ServiceRegistrationResult result = ServiceRegistrationResult.getDefaultInstance();
                    Status status = status(Result.OKAY);
                    socket.send(result.toBuilder().setUuid(uuid.toString()).setStatus(status).build().toByteArray(), 0);
                    toPublish.offer(registration);
                } catch (Exception e) {
                    String message = String.format("Failed de-serializing request: %s: %s",
                                                   e.getClass().getName(), e.getMessage());
                    logger.error("Failed de-serializing request", e);
                    socket.send(result(status(Result.BAD_REQUEST, message)).toByteArray());
                }
            }
            //close();
        }

        public void close() {
            context.destroySocket(socket);
        }
    }

    class RegistryPublisher implements RunnableClosable {
        ZMQ.Socket publisher;

        @Override public void run() {
            publisher = context.createSocket(ZMQ.PUB);
            publisher.bind("tcp://*:" + DiscoveryConstants.SERVICE_REGISTRATION_PUB);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ServiceRegistration serviceRegistration = toPublish.take();
                    publisher.sendMore(serviceRegistration.getGroupName());
                    publisher.send(serviceRegistration.toByteArray());
                } catch (InterruptedException e) {
                    logger.trace("Interrupted taking ServiceRegistrations", e);
                }
            }
        }

        public void close() {
            context.destroySocket(publisher);
        }
    }

    class ServiceLookup implements RunnableClosable {
        ZMQ.Socket socket;

        @Override public void run() {
            socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:" + DiscoveryConstants.SERVICE_LOOKUP);
            ServiceRegistrationMatcher matcher = new ServiceRegistrationMatcher();
            while (!Thread.currentThread().isInterrupted()) {
                byte[] recv = socket.recv(0);
                ServiceTemplate template;
                try {
                    template = ServiceTemplate.parseFrom(recv);
                    List<ServiceRegistration> matched = new ArrayList<>();
                    matched.addAll(registrations.entrySet()
                                       .stream()
                                       .filter(entry -> matcher.match(template, entry.getValue()))
                                       .map(Map.Entry<UUID, ServiceRegistration>::getValue)
                                       .collect(Collectors.toList()));
                    Status status = status(Result.OKAY);
                    LookupResult result = LookupResult.newBuilder().setStatus(status).addAllServiceRegistration(matched).build();
                    socket.send(result.toByteArray(), 0);
                } catch (Exception e) {
                    String message = String.format("Failed de-serializing request: %s: %s",
                                                   e.getClass().getName(), e.getMessage());
                    logger.error("Failed de-serializing request", e);
                    socket.send(result(status(Result.BAD_REQUEST, message)).toByteArray());
                }
            }
            //close();
        }

        public void close() {
            context.destroySocket(socket);
        }
    }
}
