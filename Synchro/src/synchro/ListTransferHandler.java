package synchro;

/*
 *		Drag and Drop 
 */ 

import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.Iterator;
import java.util.List;

public class ListTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
 
    //	authorise import
    
    public boolean canImport(TransferHandler.TransferSupport info) {
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        
        return true;
   }

    //	export: bundles up the data to be exported into a Transferable
    //  object in preparation for the transfer
    
    protected Transferable createTransferable(JComponent c) {
        return new StringSelection(exportString(c));
    }
 
    //	export: support COPY and MOVE ?
    
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    //	import: called on a successful drop (or paste) and initiates the
    //	transfer of data to the target component.
    //	This method returns true if the import was successful false otherwise
    
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }

        JList list = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel)list.getModel();
        JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
        int index = dl.getIndex();
        boolean insert = dl.isInsert();

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        String data;
        try {
            data = (String)t.getTransferData(DataFlavor.stringFlavor);
        } 
        catch (Exception e) { return false; }
        String[] values = data.split("\n");
                                
        // Perform the actual import.  
        if (insert) {
            for (int i = 0; i < values.length; i++) {
            	if ( !listModel.contains(values[i]) )
            		listModel.add(index++, values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
            	if ( !listModel.contains(values[i]) )
            		listModel.set(index++, values[i]);
            }
        }
        return true;
    }

    //	 invoked after the export is complete.
    //   When the action is a MOVE, the data needs to be removed
    //   from the source after the transfer is complete
    
    protected void exportDone(JComponent c, Transferable data, int action) {
    }

    //	Bundle up the selected items in the list
    //	as a single string, for export.
    
    protected String exportString(JComponent c) {
        JList list = (JList)c;
        List values = list.getSelectedValuesList();       
        StringBuffer buff = new StringBuffer();       
        Iterator it = values.iterator();
        while ( it.hasNext() )  {
            Object val = it.next();
            buff.append(val == null ? "" : val.toString());
            buff.append("\n");
        }       
        return buff.toString();
    }
    
}
