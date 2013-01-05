package org.sonatype.aether.connector.file;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * Wrapper object for {@link ArtifactTransfer} and {@link MetadataTransfer} objects.
 * 
 * @author Benjamin Hanzelmann
 */
class TransferWrapper
{

    public enum Type
    {
        ARTIFACT, METADATA
    }

    private Type type;

    public Type getType()
    {
        return type;
    }

    private MetadataTransfer metadataTransfer;

    private ArtifactTransfer artifactTransfer;

    private Transfer transfer;

    private String checksumPolicy = null;

    private boolean existenceCheck = false;

    public TransferWrapper( ArtifactTransfer transfer )
    {
        super();
        this.artifactTransfer = transfer;
        this.transfer = transfer;
        this.type = Type.ARTIFACT;

        if ( transfer instanceof ArtifactDownload )
        {
            this.checksumPolicy = ( (ArtifactDownload) transfer ).getChecksumPolicy();
            this.existenceCheck = ( (ArtifactDownload) transfer ).isExistenceCheck();
        }
    }

    public TransferWrapper( MetadataTransfer transfer )
    {
        super();
        this.metadataTransfer = transfer;
        this.transfer = transfer;
        this.type = Type.METADATA;

        if ( transfer instanceof MetadataDownload )
        {
            this.checksumPolicy = ( (MetadataDownload) transfer ).getChecksumPolicy();
        }
    }

    public void setState( State new1 )
    {
        transfer.setState( new1 );
    }

    public File getFile()
    {
        File ret = null;

        if ( metadataTransfer != null )
        {
            ret = metadataTransfer.getFile();
        }
        else if ( artifactTransfer != null )
        {
            ret = artifactTransfer.getFile();
        }

        if ( ret == null )
        {
            if ( metadataTransfer != null )
            {
                ret = metadataTransfer.getMetadata().getFile();
            }
            else if ( artifactTransfer != null )
            {
                ret = artifactTransfer.getArtifact().getFile();
            }
        }

        return ret;

    }

    public Artifact getArtifact()
    {
        if ( artifactTransfer != null )
        {
            return artifactTransfer.getArtifact();
        }
        throw new IllegalStateException( "TransferWrapper holds the wrong type" );

    }

    public void setException( ArtifactTransferException exception )
    {
        if ( artifactTransfer != null )
        {
            artifactTransfer.setException( exception );
        }
        else
        {
            throw new IllegalStateException( "TransferWrapper holds the wrong type" );
        }
    }

    public void setException( MetadataTransferException exception )
    {
        if ( metadataTransfer != null )
        {
            metadataTransfer.setException( exception );
        }
        else
        {
            throw new IllegalStateException( "TransferWrapper holds the wrong type" );
        }
    }

    public Exception getException()
    {
        if ( artifactTransfer != null )
        {
            return artifactTransfer.getException();
        }
        else if ( metadataTransfer != null )
        {
            return metadataTransfer.getException();
        }
        else
        {
            throw new IllegalStateException( "TransferWrapper holds the wrong type" );
        }
    }

    public Metadata getMetadata()
    {
        return metadataTransfer.getMetadata();
    }

    public String getChecksumPolicy()
    {
        return this.checksumPolicy;
    }

    public boolean isExistenceCheck()
    {
        return existenceCheck;
    }

}
