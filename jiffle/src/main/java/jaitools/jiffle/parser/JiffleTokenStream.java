/*
 * Copyright 2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.jiffle.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.antlr.runtime.BufferedTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * A {@code TokenStream} implementation for the Jiffle script parsers. It is
 * adapted from ANTLR's {@link org.antlr.runtime.CommonTokenStream} class but
 * allows the parser to receive tokens from more than more than one channel.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public final class JiffleTokenStream extends BufferedTokenStream {

    protected final List<Integer> activeChannels = new ArrayList<Integer>();

    public JiffleTokenStream(TokenSource tokenSource) {
        super(tokenSource);
        addActiveChannel( Token.DEFAULT_CHANNEL );
    }

    public JiffleTokenStream(TokenSource tokenSource, int[] channels) {
        super(tokenSource);
        for (int i = 0; i < channels.length; i++) {
            addActiveChannel(channels[i]);
        }
    }

    @Override
    public void consume() {
        if ( p == -1 ) setup();
        p++;
        sync(p);
        while ( !isActiveChannel( tokens.get(p).getChannel() ) ) {
            p++;
            sync(p);
        }
    }

    @Override
    protected Token LB(int k) {
        if ( k==0 || (p-k)<0 ) return null;

        int i = p;
        int n = 1;
        // find k good tokens looking backwards
        while ( n<=k ) {
            // skip off-channel tokens
            i = skipOffTokenChannelsReverse(i-1);
            n++;
        }
        if ( i<0 ) return null;
        return tokens.get(i);
    }

    @Override
    public Token LT(int k) {
        //System.out.println("enter LT("+k+")");
        if ( p == -1 ) setup();
        if ( k == 0 ) return null;
        if ( k < 0 ) return LB(-k);
        int i = p;
        int n = 1; // we know tokens[p] is a good one
        // find k good tokens
        while ( n<k ) {
            // skip off-channel tokens
            i = skipOffTokenChannels(i+1);
            n++;
        }
		if ( i>range ) range = i;
        return tokens.get(i);
    }

    /** Given a starting index, return the index of the first on-channel
     *  token.
     */
    protected int skipOffTokenChannels(int i) {
        sync(i);
        while ( !isActiveChannel( tokens.get(i).getChannel() ) ) {
            i++;
            sync(i);
        }
        return i;
    }

    protected int skipOffTokenChannelsReverse(int i) {
        while ( i>=0 && !isActiveChannel((tokens.get(i)).getChannel()) ) {
            i--;
        }
        return i;
    }

    @Override
    protected void setup() {
        p = 0;
        sync(0);
        int i = 0;
        while ( !isActiveChannel( tokens.get(i).getChannel() ) ) {
            i++;
            sync(i);
        }
        p = i;
    }

    @Override
    public void setTokenSource(TokenSource tokenSource) {
        synchronized (activeChannels) {
            super.setTokenSource(tokenSource);
            removeAllActiveChannels();
            addActiveChannel(Token.DEFAULT_CHANNEL);
        }
    }

    public void addActiveChannel(int channelNum) {
        synchronized (activeChannels) {
            if (!isActiveChannel(channelNum)) {
                activeChannels.add(channelNum);
            }
        }
    }

    public void removeActiveChannel(int channelNum) {
        synchronized (activeChannels) {
            Iterator<Integer> iter = activeChannels.iterator();
            while (iter.hasNext()) {
                if (iter.next() == channelNum) {
                    iter.remove();
                }
            }
        }
    }

    public boolean isActiveChannel(int channelNum) {
        synchronized (activeChannels) {
            return activeChannels.contains(channelNum);
        }
    }

    public void removeAllActiveChannels() {
        synchronized (activeChannels) {
            activeChannels.clear();
        }
    }
}

