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
package org.mstc.zmq.json.test;

import org.mstc.zmq.json.Decoder;
import org.mstc.zmq.json.Encoder;

import java.io.IOException;

/**
 * @author Dennis Reedy
 */
public class Input {
    private String input;
    private double[] doubles;
    private int[] ints;
    private float[] floats;
    private String[] strings;

    public String getInput() {
        return input;
    }

    public double[] getDoubles() {
        return doubles;
    }

    public int[] getInts() {
        return ints;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Input parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, Input.class.getDeclaredFields(), b);
        return b.build();
    }

    public static Input parseFrom(String input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, Input.class.getDeclaredFields(), b);
        return b.build();
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static class Builder {
        Input input = new Input();

        public Builder setInput(String input) {
            this.input.input = input;
            return this;
        }

        public Builder setDoubles(double[] d) {
            input.doubles = new double[d.length];
            System.arraycopy(d, 0, input.doubles, 0, d.length);
            return this;
        }

        public Builder setInts(int[] i) {
            input.ints = new int[i.length];
            System.arraycopy(i, 0, input.ints, 0, i.length);
            return this;
        }

        public Builder setFloats(float[] f) {
            input.floats = new float[f.length];
            System.arraycopy(f, 0, input.floats, 0, f.length);
            return this;
        }

        public Builder setStrings(String[] s) {
            input.strings = new String[s.length];
            System.arraycopy(s, 0, input.strings, 0, s.length);
            return this;
        }

        public Input build() {
            return input;
        }

    }
}
