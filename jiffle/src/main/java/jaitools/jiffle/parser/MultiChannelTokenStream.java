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
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * A {@code TokenStream} that can work with multiple active channels.
 * Adapted from ANTLR's {@link org.antlr.runtime.CommonTokenStream} class.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public final class MultiChannelTokenStream extends BufferedTokenStream {

    /**
     * The {@code List} of active channel indices. These should all be positive
     * and less than 99 (used by ANTLR to flag its hidden token channel).
     */
    protected final List<Integer> activeChannels = new ArrayList<Integer>();

    /**
     * Creates a new stream with the default ANTLR channel active.
     * 
     * @param tokenSource a lexer
     */
    public MultiChannelTokenStream(TokenSource tokenSource) {
        super(tokenSource);
        addActiveChannel(Token.DEFAULT_CHANNEL);
    }

    /**
     * Creates a new stream with the given channels active.
     * 
     * @param tokenSource a lexer
     * @param channels active channel indices
     */
    public MultiChannelTokenStream(TokenSource tokenSource, int[] channels) {
        super(tokenSource);
        for (int i = 0; i < channels.length; i++) {
            addActiveChannel(channels[i]);
        }
    }

    @Override
    public void consume() {
        if (p == -1) {
            setup();
        }
        p++;
        sync(p);
        while (!isActiveChannel(tokens.get(p).getChannel())) {
            p++;
            sync(p);
        }
    }

    /**
     * Looks backwards for the {@code kth} token on any of the active channels.
     * 
     * @param k number of active-channel tokens to scan over
     * @return the token
     */
    @Override
    protected Token LB(int k) {
        if (k == 0 || (p - k) < 0) {
            return null;
        }
        
        CommonTokenStream cs;

        int i = p;
        int n = 1;
        while (n <= k) {
            i = skipOffTokenChannelsReverse(i - 1);
            n++;
        }
        if (i < 0) {
            return null;
        }
        return tokens.get(i);
    }

    /**
     * Looks forwards for the {@code kth} token on any of the active channels.
     * 
     * @param k number of active-channel tokens to scan over
     * @return the token
     */
    @Override
    public Token LT(int k) {
        if (p == -1) {
            setup();
        }
        if (k == 0) {
            return null;
        }
        if (k < 0) {
            return LB(-k);
        }
        int i = p;
        int n = 1;
        while (n < k) {
            i = skipOffTokenChannels(i + 1);
            n++;
        }
        if (i > range) {
            range = i;
        }
        return tokens.get(i);
    }

    /**
     * Gets the index of the next token on an active channel, starting
     * from {@code pos}.
     * 
     * @param pos start token index
     * 
     * @return the token index 
     */
    protected int skipOffTokenChannels(int pos) {
        sync(pos);
        while (!isActiveChannel(tokens.get(pos).getChannel())) {
            pos++;
            sync(pos);
        }
        return pos;
    }

    /**
     * Gets the index of the next token on an active channel, starting
     * from {@code pos} and scanning backwards.
     * 
     * @param pos start token index
     * 
     * @return the token index 
     */
    protected int skipOffTokenChannelsReverse(int pos) {
        while (pos >= 0 && !isActiveChannel((tokens.get(pos)).getChannel())) {
            pos--;
        }
        return pos;
    }

    /**
     * Positions the stream at the first token on an active channel.
     */
    @Override
    protected void setup() {
        p = 0;
        sync(0);
        int i = 0;
        while (!isActiveChannel(tokens.get(i).getChannel())) {
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

    /**
     * Adds a channel to those active.
     * @param channelNum the channel to add
     */
    public void addActiveChannel(int channelNum) {
        synchronized (activeChannels) {
            if (!isActiveChannel(channelNum)) {
                activeChannels.add(channelNum);
            }
        }
    }

    /**
     * Removes a channel from those active. It is safe to call this
     * method speculatively.
     * 
     * @param channelNum the channel to remove
     */
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

    /**
     * Tests if a channel is active.
     * 
     * @param channelNum the channel to test
     * @return {@code true} if the channel is active; {@code false otherwise}
     */
    public boolean isActiveChannel(int channelNum) {
        synchronized (activeChannels) {
            return activeChannels.contains(channelNum);
        }
    }

    /**
     * Removes all active channels.
     */
    public void removeAllActiveChannels() {
        synchronized (activeChannels) {
            activeChannels.clear();
        }
    }
}
