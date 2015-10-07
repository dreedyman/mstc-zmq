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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dennis Reedy
 */
public class Decoder {
    static Logger logger = LoggerFactory.getLogger(Decoder.class);

    public static void decode(byte[] input, Field[] fields, Object b) throws IOException {
        decode(new String(input), fields, b);
    }

    @SuppressWarnings("unchecked")
    public static void decode(String input, Field[] fields, Object b) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        if(logger.isDebugEnabled())
            logger.debug(input);
        JsonFactory factory = mapper.getFactory();
        try (JsonParser jp = factory.createParser(input)) {
            /* Sanity check: verify that we got "Json Object" */
            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object");
            }

            /* Iterate over object fields */
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                jp.nextToken();
                Field field = getField(fieldName, fields);
                if(field==null) {
                    throw new IOException("Could not find field ["+fieldName+"] on class "+b.getClass().getName());
                }
                try {
                    if(field.getType().isAssignableFrom(List.class)) {
                        String adder = getAdder(fieldName);
                        TypeFactory t = TypeFactory.defaultInstance();
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                        List list = mapper.readValue(jp.getValueAsString(), t.constructCollectionType(List.class, listClass));
                        Method m = b.getClass().getDeclaredMethod(adder, Collection.class);
                        m.invoke(b, list);
                    } else if(field.getType().isArray()) {
                        Class<?> type = field.getType();
                        String setter = getSetter(fieldName);
                        Method m = b.getClass().getDeclaredMethod(setter, field.getType());
                        logger.info("Field {} is array of {}[], {}, using method {}",
                                    field.getName(),
                                    field.getType().getComponentType(),
                                    jp.getCurrentToken().name(),
                                    m);
                        if(jp.getCurrentToken().id()==JsonToken.START_ARRAY.id()) {
                            List list = new ArrayList();
                            while (jp.nextToken() != JsonToken.END_ARRAY) {
                                String value = jp.getText();
                                switch(jp.getCurrentToken()) {
                                    case VALUE_STRING:
                                        list.add(value);
                                        break;
                                    case VALUE_NUMBER_INT:
                                        if(type.getComponentType().isAssignableFrom(double.class)) {
                                            list.add(Double.parseDouble(value));
                                        } else if(type.getComponentType().isAssignableFrom(float.class)) {
                                            list.add(Float.parseFloat(value));
                                        } else {
                                            list.add(Integer.parseInt(value));
                                        }
                                        break;
                                    case VALUE_NUMBER_FLOAT:
                                        logger.info("Add float");
                                        list.add(jp.getFloatValue());
                                        break;
                                    case VALUE_NULL:
                                        break;
                                    default:
                                        logger.warn("[3] Not sure how to handle {} yet", jp.getCurrentToken().name());
                                }
                            }
                            Object array = Array.newInstance(field.getType().getComponentType(), list.size());
                            for (int i = 0; i < list.size(); i++) {
                                Object val = list.get(i);
                                Array.set(array, i, val);
                            }
                            m.invoke(b, array);
                        } else {
                            if (type.getComponentType().isAssignableFrom(byte.class)) {
                                m.invoke(b, jp.getBinaryValue());
                            }
                        }
                    } else {
                        String setter = getSetter(fieldName);
                        logger.debug("{}: {}", setter, field.getType().getName());
                        Method m = b.getClass().getDeclaredMethod(setter, field.getType());

                        switch(jp.getCurrentToken()) {
                            case VALUE_STRING:
                                m.invoke(b, jp.getText());
                                break;
                            case VALUE_NUMBER_INT:
                                m.invoke(b, jp.getIntValue());
                                break;
                            case VALUE_NUMBER_FLOAT:
                                m.invoke(b, jp.getFloatValue());
                                break;
                            case VALUE_NULL:
                                logger.debug("Skip invoking {}.{}, property is null", b.getClass().getName(), m.getName());
                                break;
                            case START_OBJECT:
                                StringBuilder sb = new StringBuilder();
                                while (jp.nextToken() != JsonToken.END_OBJECT) {
                                    switch(jp.getCurrentToken()) {
                                        case VALUE_STRING:
                                            sb.append("\"").append(jp.getText()).append("\"");
                                            break;
                                        case FIELD_NAME:
                                            if(sb.length()>0)
                                                sb.append(",");
                                            sb.append("\"").append(jp.getText()).append("\"").append(":");
                                            break;
                                        case VALUE_NUMBER_INT:
                                            sb.append(jp.getIntValue());
                                            break;
                                        case VALUE_NUMBER_FLOAT:
                                            sb.append(jp.getFloatValue());
                                            break;
                                        case VALUE_NULL:
                                            sb.append("null");
                                            break;
                                        default:
                                            logger.warn("[2] Not sure how to handle {} yet", jp.getCurrentToken().name());
                                    }
                                }
                                String s = String.format("%s%s%s",
                                                         JsonToken.START_OBJECT.asString(),
                                                         sb.toString(),
                                                         JsonToken.END_OBJECT.asString());
                                Object parsed = getNested(field.getType(), s.getBytes());
                                m.invoke(b, parsed);
                                break;
                            default:
                                logger.warn("[1] Not sure how to handle {} yet", jp.getCurrentToken().name());
                        }
                    }
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
                    logger.error("Failed setting field [{}], builder: {}", fieldName, b.getClass().getName(), e);
                }
            }
        }
    }

    static <T> T[] toArray(List<T> list) {
        Class clazz = list.get(0).getClass(); // check for size and null before
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    static Object getNested(Class<?> nested, byte[] input) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method parseFrom = nested.getMethod("parseFrom", byte[].class);
        return parseFrom.invoke(null, input);

    }

    static Field getField(String name, Field[] fields) {
        for(Field f :fields) {
            if(f.getName().equals(name))  {
                return f;
            }
        }
        return null;
    }

    static String getSetter(String fieldName) {
        return getMethod("set", fieldName);
    }

    static String getAdder(String fieldName) {
        return getMethod("add", fieldName);
    }

    static String getMethod(String action, String fieldName) {
        String prefix = fieldName.substring(0,1).toUpperCase();
        String suffix = fieldName.substring(1, fieldName.length());
        return String.format("%s%s%s", action, prefix, suffix);
    }
}
