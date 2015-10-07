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
public class Output {
    private String output;
    private  double[] doubles;
    private int[] ints;

    public String getOutput() {
        return output;
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

    public static Output parseFrom(byte[] input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, Output.class.getDeclaredFields(), b);
        return b.build();
    }

    public static Output parseFrom(String input) throws IOException {
        Builder b = new Builder();
        Decoder.decode(input, Output.class.getDeclaredFields(), b);
        return b.build();
    }

    public byte[] toByteArray() throws IOException {
        return Encoder.encode(this).getBytes();
    }

    public static class Builder {
        Output output = new Output();

        public Builder setOutput(String output) {
            this.output.output = output;
            return this;
        }

        public Builder setDoubles(double[] d) {
            output.doubles = new double[d.length];
            System.arraycopy(d, 0, output.doubles, 0, d.length);
            return this;
        }

        public Builder setInts(int[] i) {
            output.ints = new int[i.length];
            System.arraycopy(i, 0, output.ints, 0, i.length);
            return this;
        }

        public Output build() {
            return output;
        }

    }
}
