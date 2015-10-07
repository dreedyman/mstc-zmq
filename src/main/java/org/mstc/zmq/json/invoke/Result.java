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
package org.mstc.zmq.json.invoke;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Dennis Reedy
 */
public enum Result {
    OKAY(0),
    NO_SERVICE(1),
    NO_METHOD(2),
    BAD_REQUEST(3),
    INVOCATION_ERROR(4);

    private final int value;

    Result(int v) {
        value = v;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static Result fromValue(int typeCode) {
        for (Result c : Result.values()) {
            if (c.value == typeCode) {
                return c;
            }
        }
        throw new IllegalArgumentException("Invalid Result type code: " + typeCode);
    }
}
