package org.nakedobjects.object.snapshot;


import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

public class DomSerializerJaxp implements DomSerializer {

    public String serialize(Element domElement) {
        CharArrayWriter caw = new CharArrayWriter();
        try {
            serializeTo(domElement, caw);
            return caw.toString();
        } catch(IOException ex) {
            return null;
        }
    }

    public void serializeTo(final Element domElement, OutputStream os) throws IOException {
        CharArrayWriter caw = new CharArrayWriter();
        StreamResult result = new StreamResult(os);
        serializeTo(domElement, result);
    }

    public void serializeTo(final Element domElement, Writer w) throws IOException {
        StreamResult result = new StreamResult(w);
        serializeTo(domElement, new StreamResult(w));
    }

    private void serializeTo(final Element domElement, Result result) throws IOException {
        DOMSource source = new DOMSource(domElement);
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (TransformerException e) {
            throw new IOException(e.getMessage());
        }
    }
    
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/