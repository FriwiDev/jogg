/*****************************************************************************************
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
 ****************************************************************************************/

package org.echocat.jogg;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created by christian.rijke on 14.08.2014.
 */
public class OggDecoderTest {

    Logger LOG = LoggerFactory.getLogger(OggDecoderTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void decodeOggHeader() throws IOException {
        try (final InputStream is = getClass().getResourceAsStream("bach_48k.ogg");
             final OggSyncStateInput ssi = new OggSyncStateInput(is)) {

            assertThat(ssi.hasNext(), is(true));

            OggPageInput header1 = ssi.next();
            assertThat(header1.getNumberOfPackets(), is(1));
            assertThat(header1.isBos(), is(true));

            assertThat(ssi.hasNext(), is(true));

            OggPageInput header2 = ssi.next();
            assertThat(header2.getNumberOfPackets(), is(1));
        }
    }

    @Test
    public void decodeOggPages() throws IOException {
        try (final InputStream is = getClass().getResourceAsStream("bach_48k.ogg");
             final OggSyncStateInput ssi = new OggSyncStateInput(is)) {

            assertThat(ssi.hasNext(), is(true));

            OggPageInput oggPageInput = null;
            long previousPageNo = -1L;
            while (ssi.hasNext()) {
                oggPageInput = ssi.next();
                LOG.info("page no: {}, number of packets: {}, granulepos: {},  bos: {}, eos: {}", oggPageInput.getPageno(), oggPageInput.getNumberOfPackets(), oggPageInput.getGranulepos(), oggPageInput.isBos(), oggPageInput.isEos());
                assertThat(oggPageInput.getPageno(), is(previousPageNo + 1));
                previousPageNo++;
            }
            assertThat(oggPageInput.getPageno(), is(12L));
            assertThat(oggPageInput.isEos(), is(true));
        }
    }

    @Test
    public void decodeOggPackets() throws IOException {
        try (final InputStream is = getClass().getResourceAsStream("bach_48k.ogg");
             final OggSyncStateInput ssi = new OggSyncStateInput(is)) {

            assertThat(ssi.hasNext(), is(true));

            OggPacket oggPacket = null;
            OggPageInput oggPageInput;
            long previousPacketNo = -1L;
            while (ssi.hasNext()) {
                oggPageInput = ssi.next();
                LOG.info("page no: {}, number of packets: {}, granulepos: {},  bos: {}, eos: {}", oggPageInput.getPageno(), oggPageInput.getNumberOfPackets(), oggPageInput.getGranulepos(), oggPageInput.isBos(), oggPageInput.isEos());

                while (oggPageInput.hasNext()) {
                    oggPacket = oggPageInput.next();
                    assertThat(oggPacket.getPacketno(), is(previousPacketNo + 1));
                    previousPacketNo++;
                }
            }
            assertThat(oggPacket.getPacketno(), is(534L));
        }
    }

}