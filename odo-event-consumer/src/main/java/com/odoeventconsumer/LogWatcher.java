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
import java.util.Arrays;
import java.util.List;

/**
 * A utility class which calls 'odo log (component)' in order to output the application logs (from odo) to the console.
 */
public class LogWatcher {

    private final OdoContext context;

    private Process currentProcess;

    public LogWatcher(OdoContext context) {
        this.context = context;
    }

    void start() throws IOException {

        ProcessBuilder pb = new ProcessBuilder();

        pb.directory(context.getContextPath().toFile());

        List<String> command = new ArrayList<>(Arrays.asList(context.getOdoBinaryPath().toString(), "-f"));

        pb.command(command);

        this.currentProcess = pb.start();

        new InputStreamReaderThread(this.currentProcess.getInputStream(), false).start();
        new InputStreamReaderThread(this.currentProcess.getErrorStream(), true).start();

    }

    /**
     * Read from the process input stream and write with the appropriate stream name (stderr/stdout)
     */
    private static class InputStreamReaderThread extends Thread {

        private final InputStream is;
        private final boolean stderr;

        public InputStreamReaderThread(InputStream is, boolean stderr) {
            this.is = is;
            this.stderr = stderr;
        }

        @Override
        public void run() {

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String str;
            try {
                while (null != (str = br.readLine())) {

                    if (stderr) {
                        System.out.println("[err] " + str);
                    } else {
                        System.out.println("[out] " + str);
                    }

                }
            } catch (IOException e) {
                // ignore

            }

        }

    }

}
