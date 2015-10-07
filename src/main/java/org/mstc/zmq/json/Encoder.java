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
package org.mstc.zmq.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Dennis Reedy
 */
public class Encoder {
    static Logger logger = LoggerFactory.getLogger(Encoder.class);

    public static String encode(Object o) throws IOException {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonFactory factory = mapper.getFactory();
        try (JsonGenerator g = factory.createGenerator(writer)) {
            g.writeStartObject();
            for (Field f : o.getClass().getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object value = f.get(o);
                    if (value != null) {
                        if (f.getType().isAssignableFrom(List.class)) {
                            String items = mapper.writeValueAsString(value);
                            g.writeStringField(f.getName(), items);
                        } else if (f.getType().isArray()) {
                            if(f.getType().getComponentType().isAssignableFrom(byte.class)) {
                                g.writeBinaryField(f.getName(), (byte[])value);
                            } else {
                                int length = Array.getLength(value);
                                g.writeFieldName(f.getName());
                                g.writeStartArray();
                                for (int i = 0; i < length; i++) {
                                    Object av = Array.get(value, i);
                                    if(av instanceof Double) {
                                        g.writeNumber(new BigDecimal((Double) av).toPlainString());
                                    } else if(av instanceof Float) {
                                        g.writeNumber(new BigDecimal((Float) av).toPlainString());
                                    } else if(av instanceof Integer) {
                                        g.writeNumber(new BigDecimal((Integer)av).toPlainString());
                                    } else {
                                        g.writeObject(av);
                                    }
                                    /*if (av instanceof Double)
                                        g.writeNumber(new BigDecimal((Double) av));
                                    else if (av instanceof Float)
                                        g.writeNumber(new BigDecimal((Float) av));
                                    else if (av instanceof Integer)
                                        g.writeNumber((Integer) av);*/
                                }
                                g.writeEndArray();
                            }
                        } else {
                            g.writeObjectField(f.getName(), value);
                        }
                    }

                } catch (IllegalAccessException e) {
                    logger.warn("Could not get field: {}", f.getName(), e);
                }
            }
            g.writeEndObject();
        }
        if(logger.isDebugEnabled())
            logger.debug(writer.toString());
        return writer.toString();
    }
}
