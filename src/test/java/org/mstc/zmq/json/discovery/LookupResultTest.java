package org.mstc.zmq.json.discovery;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;

public class LookupResultTest {
    String address;

    @Before
    public void setup() throws UnknownHostException {
        address = InetAddress.getLocalHost().getHostAddress();
    }

    @Test
    public void testParseFrom() throws Exception {
        LookupResult lr1 = LookupResult.newBuilder()
                               .addServiceRegistration(create("Luke", "1234"))
                               .addServiceRegistration(create("Darth", "5678"))
                               .addServiceRegistration(create("Vader", "9876")).build();
        String json1 = lr1.toJSON();
        LookupResult lr2 = LookupResult.parseFrom(json1.getBytes());
        assertTrue(lr2.getServiceRegistrationList().size()==3);
        System.out.println(json1);
        System.out.println(lr2.toJSON());
        assertTrue(json1.equals(lr2.toJSON()));
    }

    ServiceRegistration create(String name, String port) {
        return ServiceRegistration.newBuilder()
                   .setName(name)
                   .setGroupName("test")
                   .setEndPoint("tcp://" + address + ":" + port)
                   .addMethodName("foo")
                   .addMethodName("bar").build();
    }

    @Test
    public void testToByteArray() throws Exception {

    }
}