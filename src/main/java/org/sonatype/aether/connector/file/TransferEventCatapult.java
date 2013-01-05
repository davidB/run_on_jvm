package org.sonatype.aether.connector.file;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

/**
 * Helper for {@link TransferEvent}-handling.
 * 
 * @author Benjamin Hanzelmann
 */
class TransferEventCatapult
{

    private TransferListener listener;

    public TransferEventCatapult( TransferListener listener )
    {
        if ( listener == null )
        {
            this.listener = new NoTransferListener();
        }
        else
        {
            this.listener = listener;
        }
    }

    protected void fireInitiated( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.INITIATED );
        listener.transferInitiated( event );
    }

    protected void fireStarted( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.STARTED );
        listener.transferStarted( event );
    }

    protected void fireSucceeded( DefaultTransferEvent event )
    {
        event.setType( TransferEvent.EventType.SUCCEEDED );
        listener.transferSucceeded( event );
    }

    protected void fireFailed( DefaultTransferEvent event )
    {
        event.setType( TransferEvent.EventType.FAILED );
        listener.transferFailed( event );
    }

    protected void fireCorrupted( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.FAILED );
        listener.transferCorrupted( event );
    }

    protected void fireProgressed( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.PROGRESSED );
        listener.transferProgressed( event );
    }

    private final class NoTransferListener
        extends AbstractTransferListener
    {
    }

}
