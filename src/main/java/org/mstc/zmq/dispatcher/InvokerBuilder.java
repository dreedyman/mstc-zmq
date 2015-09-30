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
import com.google.protobuf.Message;
import org.mstc.zmq.Discovery.ServiceTemplate;
import org.mstc.zmq.Invoke.MethodRequest;
import org.mstc.zmq.Invoke.MethodResult;

/**
 * @author Dennis Reedy
 */
public class InvokerBuilder {
    private static Invoker invoker = new Invoker();

    public static MethodRequest method(String method, Class<?> type, byte[] input) {
        return MethodRequest.newBuilder()
            .setMethod(method)
            .setType(type.getName())
            .setInput(ByteString.copyFrom(input)).build();
    }

    public static MethodRequest method(String method) {
        return MethodRequest.newBuilder().setMethod(method).build();
    }

    public static MethodRequest method(String method, MethodRequest input) {
        return MethodRequest.newBuilder()
                   .setMethod(method)
                .setType(input.getType())
                .setInput(input.getInput()).build();
    }

    public static MethodRequest input(Class<?> type, Message message) {
        return MethodRequest.newBuilder()
                .setType(type.getName())
                .setInput(message.toByteString()).buildPartial();
    }

    public static Invoker service(String name, MethodRequest request) {
        ServiceTemplate template =  ServiceTemplate.newBuilder().setName(name).build();
        return getOne(request, template);
    }

    public static Invoker service(String name, Class<?> type, MethodRequest request) {
        ServiceTemplate template = ServiceTemplate.newBuilder().setName(name).setInterface(type.getName()).build();
        return getOne(request, template);
    }

    public static Invoker service(Class<?> type, MethodRequest request) {
        ServiceTemplate template =  ServiceTemplate.newBuilder().setInterface(type.getName()).build();
        return getOne(request, template);
    }

    public static Invoker service(Class<?> type, String group, MethodRequest request) {
        ServiceTemplate template = ServiceTemplate.newBuilder().setInterface(type.getName()).setGroupName(group).build();
        return getOne(request, template);
    }

    public static Invoker service(String name, Class<?> type, String group, MethodRequest request) {
        ServiceTemplate template = ServiceTemplate.newBuilder()
                .setName(name)
                .setInterface(type.getName())
                .setGroupName(group).build();
        return getOne(request, template);
    }

    public static MethodResult output(Class<?> type) {
        return MethodResult.newBuilder().setType(type.getName()).build();
    }

    public static void shutdown() {
        invoker.shutdown();
    }

    private static Invoker getOne(MethodRequest methodRequest, ServiceTemplate template) {
        invoker.push(methodRequest, template);
        return invoker;
    }

}
