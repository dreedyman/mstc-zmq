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

import com.google.protobuf.ByteString;
import org.mstc.zmq.annotations.HandlerNotify;
import org.mstc.zmq.annotations.Remoteable;
import org.mstc.zmq.Discovery.ServiceRegistration;
import org.mstc.zmq.Invoke.MethodRequest;
import org.mstc.zmq.Invoke.MethodResult;
import org.mstc.zmq.Invoke.Result;
import org.mstc.zmq.Invoke.Status;
import org.mstc.zmq.discovery.ServiceRegistrationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dennis Reedy
 */
public class Dispatcher implements Handler {
    private ZContext context;
    private ZMQ.Socket socket;
    private String endPoint;
    private ServiceRegistrationClient serviceRegistrationClient;
    private final LinkedList<Bean> beans = new LinkedList<>();
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    public Dispatcher() {
        context = new ZContext(1);
        serviceRegistrationClient = new ServiceRegistrationClient(context);
        socket = context.createSocket(ZMQ.REP);
        String address;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to get localhost", e);
        }
        //socket.bind(endPoint);
        int port = socket.bindToRandomPort(String.format("tcp://%s", address));
        endPoint = String.format("tcp://%s:%s", address, port);
        logger.info("Dispatcher endpoint: {}", endPoint);
        pool.submit(new Worker());
    }

    public void register(Object o, Map<String, String> attributes) throws IOException {
        Class<?> c = o.getClass();
        Bean bean = new Bean(o);
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(Remoteable.class) != null) {
                bean.methods.add(m);
            }
            if (m.getAnnotation(HandlerNotify.class) != null) {
                try {
                    m.invoke(o, this);
                } catch (Exception e) {
                    logger.error("Unable to register lifecycle", e);
                }
            }
        }

        ServiceRegistration registration = ServiceRegistration.newBuilder()
                                               .setName(attributes.get("name"))
                                               .setGroupName(attributes.get("name"))
                                               .setInterface(attributes.get("interface"))
                                               .setDescription(attributes.get("description"))
                                               .setEndPoint(endPoint).build();

        serviceRegistrationClient.register(registration, System.getProperty("mstc.zmq.lookup"));

        beans.add(bean);
    }

    public void deregister(Object o) {
        Bean toRemove = null;
        for(Bean b : beans) {
            if(b.bean.equals(o)) {
                toRemove = b;
                break;
            }
        }
        if(toRemove!=null) {
            beans.remove(toRemove);
            logger.info("{} Invoked {} times", toRemove, toRemove.count.get());
        } else {
            logger.warn("Could not find impl in bean collection");
        }
        if (beans.isEmpty()) {
            shutdown();
        }
    }

    public void shutdown() {
        if (context != null)
            context.destroy();
        pool.shutdownNow();
    }

    class Bean {
        final Object bean;
        final Set<Method> methods = new HashSet<>();
        final AtomicInteger count = new AtomicInteger(0);

        public Bean(Object bean) {
            this.bean = bean;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Bean bean1 = (Bean) o;
            return bean.equals(bean1.bean);
        }

        @Override
        public int hashCode() {
            return bean.hashCode();
        }
    }

    class Worker implements Runnable {

        @Override public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] recv = socket.recv(0);
                MethodRequest request;
                try {
                    request = MethodRequest.parseFrom(recv);
                } catch (Exception e) {
                    String message = String.format("Failed de-serializing request: %s: %s",
                                                   e.getClass().getName(), e.getMessage());
                    logger.error("Failed de-serializing request", e);
                    socket.send(result(status(Result.BAD_REQUEST, message)).toByteArray());
                    continue;
                }
                Bean matched = null;
                Method method = null;
                for (Bean bean : beans) {
                    for (Method m : bean.methods) {
                        if (match(m, request)) {
                            matched = bean;
                            method = m;
                            break;
                        }
                    }
                    if(matched!=null) {
                        break;
                    }
                }
                if (matched != null) {
                    Object result;
                    try {
                        if (request.getInput() == null) {
                            result = method.invoke(matched.bean);
                        } else {
                            result = method.invoke(matched.bean, request.getInput().toByteArray());
                        }
                        matched.count.incrementAndGet();
                        socket.send(result((byte[])result).toByteArray());
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        String message = String.format("Failed invoking %s", method.getName());
                        logger.error("{}", message, e);
                        socket.send(result(status(Result.INVOCATION_ERROR, message)).toByteArray());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        beans.remove(matched);
                        beans.addLast(matched);
                    }
                } else {
                    String message = String.format("Unable to find matching method %s", request.getMethod());
                    socket.send(result(status(Result.NO_METHOD, message)).toByteArray());
                }
            }
        }

        private Status status(Result result, String status) {
            if(status==null)
                return Status.newBuilder().setResult(result).build();
            return Status.newBuilder().setResult(result).setStatus(status).build();
        }

        private MethodResult result(byte[] data) {
            return result(status(Result.OKAY, null), data);
        }

        private MethodResult result(Status status) {
            return result(status, null);
        }

        private MethodResult result(Status status, byte[] data) {
            if(data==null)
                return MethodResult.newBuilder().setStatus(status).build();
            return MethodResult.newBuilder()
                       .setStatus(status)
                       .setResult(ByteString.copyFrom(data)).build();
        }

        private boolean match(Method m, MethodRequest r) {
            boolean matched = false;
            if (m.getName().equals(r.getMethod())) {
                matched = true;
                /*if (r.getArgs() == null) {
                    matched = true;
                } else {
                    Class<?>[] argList = new Class<?>[r.getArgs().length];
                    for (int i = 0; i < r.getArgs().length; i++) {
                        argList[i] = r.getArgs()[i].getClass();
                    }
                    matched = Arrays.equals(argList, m.getParameterTypes());
                }*/
            }
            return matched;
        }
    }
}
