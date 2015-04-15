/**
 * **************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JOpus, Copyright (c) 2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 * **************************************************************************************
 */

package org.echocat.jogg;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by christian.rijke on 14.08.2014.
 */
public class OggCodecTest {

    Logger LOG = LoggerFactory.getLogger(OggCodecTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void encodeAndDecode() throws IOException {
        String source = "bach_48k.wav";
        LOG.info("encoding {}...", source);

        final int numberOfFrames = 2880;

        File tempFile = tempFolder.newFile(source.substring(0, source.length() - 4) + ".ogg");

        try (final InputStream is = getClass().getResourceAsStream(source);
             final DataInputStream in = new DataInputStream(is);
             final OutputStream out = new FileOutputStream(tempFile);
             final OggSyncStateOutput sso = new OggSyncStateOutput(out)) {

            final byte[] buffer = new byte[numberOfFrames];
            while (in.read(buffer) != -1) {
                OggPacket oggPacket = OggPacket.packetFor(buffer);
                sso.write(oggPacket);
            }
        }
        LOG.info("encoding finished");

        try (final FileInputStream fis = new FileInputStream(tempFile);
             final OggSyncStateInput ssi = new OggSyncStateInput(fis)) {

            assertThat(ssi.hasNext(), is(true));

            OggPacket oggPacket = null;
            OggPageInput oggPageInput;
            long previousPacketNo = -1L;
            while (ssi.hasNext()) {
                oggPageInput = ssi.next();
                // TODO this null check is required if the last page has eos == false
                if (oggPageInput != null) {
                    LOG.info("page no: {}, number of packets: {}, granulepos: {},  bos: {}, eos: {}", oggPageInput.getPageno(), oggPageInput.getNumberOfPackets(), oggPageInput.getGranulepos(), oggPageInput.isBos(), oggPageInput.isEos());

                    while (oggPageInput.hasNext()) {
                        oggPacket = oggPageInput.next();
                        assertThat(oggPacket.getPacketno(), is(previousPacketNo + 1));
                        previousPacketNo++;
                    }
                }
            }
            assertThat(oggPacket.getPacketno(), is(355L));
        }
        assertThat(tempFile.length(), is(1031982L));

    }
}
