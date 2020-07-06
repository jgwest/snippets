/*
 * Copyright 2020 Jonathan West, IBM Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
*/

package com.odoeventconsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Allows an InputStream of Strings to be processed as a java.util.stream Stream: Reads String lines from an InputStream
 * and passes them to an Iterator-backed Stream available by calling getStream()
 * 
 * Thread-safe
 */
public class InputStreamReaderStream extends Thread {

    private final InputStream is;

    private final List<String> waiting = new ArrayList<>();

    private final Stream<String> stream;

    public InputStreamReaderStream(InputStream is) {
        this.is = is;

        this.stream = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(new ISEventIterator(), Spliterator.ORDERED), false);

        setName(InputStreamReaderStream.class.getName());
        setDaemon(true);
        start();
    }

    public Stream<String> getStream() {
        return stream;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str;

        try {
            while (null != (str = br.readLine())) {
                synchronized (waiting) {
                    waiting.add(str);
                    waiting.notify();
                }
            }
        } catch (IOException e) {
            // Ignore
        } finally {
            synchronized (waiting) {
                waiting.add(null);
                waiting.notify();
            }
            System.out.println("Process terminated.");
        }
    }

    /**
     * Checks for Strings in the waiting array, and returns them via the iterator API; returns hasNext() false if the
     * underlying InputStream has closed.
     */
    private class ISEventIterator implements Iterator<String> {

        @Override
        public boolean hasNext() {

            String nextElement;

            while (true) {
                synchronized (waiting) {
                    if (waiting.size() == 0) {
                        try {
                            waiting.wait();
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                    if (waiting.size() > 0) {
                        nextElement = waiting.get(0);
                        break;
                    }
                }
            }

            return nextElement != null;
        }

        @Override
        public String next() {
            synchronized (waiting) {
                return waiting.remove(0);
            }
        }

    }
}
