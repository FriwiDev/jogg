/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JOgg, Copyright (c) 2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jogg;

import java.util.Iterator;

public class OggPageInput extends OggPageSupport implements Iterator<OggPacket> {

    private OggPacket _next;
    private Boolean _hasNext;

    protected OggPageInput() {
    }

    protected void init() {
        syncSerialno();
        OggStreamStateJNI.pagein(streamStateHandle(), handle());
        read();
    }

    @Override
    public boolean hasNext() {
        return _hasNext;
    }


    @Override
    public OggPacket next() {
        OggPacket current = _next;
        read();
        return current;
    }

    protected void read() {
        _next = new OggPacket();
        _hasNext = OggStreamStateJNI.packetout(streamStateHandle(), _next.handle());
    }

    protected void syncSerialno() {
        final long mySerialno = getSerialno();
        final long streamStateSerialno = OggStreamStateJNI.getSerialno(streamStateHandle());
        if (streamStateSerialno != mySerialno) {
            OggStreamStateJNI.resetSerialno(streamStateHandle(), mySerialno);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
