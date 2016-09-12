package com.dattlas.palantir.helper.panel;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.dattlas.palantir.helper.util.InfoReader;
import com.palantir.api.ui.TableLayouts;
import com.palantir.api.workspace.PalantirWorkspaceContext;

public class MainPanel extends JPanel implements DropTargetListener, PropertyChangeListener {	

	private static final long serialVersionUID = 8585896089213231492L;
	public static final String DRAG_FILE_HERE 			= "Arrastra aquí el archivo PDF...";
	public static final String DROP_FILE_HERE 			= "Suelta el archivo!";
	public static final String PROCESSING_DROPPED_FILE 	= "Procesando archivo...";
	public static final String SHOW_CURP_DATA			= "Show CURP data";
	public static final String IMAGE_IN_CLIPBOARD 		= "image in clipboard";
	
	private JPanel panelDropArea;   			// Drop area panel
    private JLabel labelCurpDropHereMessage;
    
    private Image curpImage;
    
    private Map<String, String> infoMap;
    
    final DropTarget droptarget;				// Variable to enable the Drag 'n' Drop functionality.
    
	private File pdfFile;
	
	public MainPanel(PalantirWorkspaceContext palantirContext) {
		
		TableLayout topPanelLayout = TableLayouts.create("p,f", "25,f", 5, 5);
		setLayout(topPanelLayout);
		
		setBackground(palantirContext.getColors().getSecondaryBackgroundColor());
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
		
		//Adding an informative label on the top.
		JLabel labelHelperTitle = palantirContext.getUiBuilders().labelBuilder()
				.text("Información de CURP")
				.fontSize(20.0f)
				.bold()
				.build();
		this.add(labelHelperTitle, "0,0,1,0");
					
		/*  Drop target configuration:
         panelDropArea              - Panel that will receive the pdf files by draging them from the operating system.
         defaultAction              - We will get the file (COPY operation) for manipulate it.
         targetListener             - The drag and drop functionalities are in "this" class, because I implemented the DropTargetListener. 
         isTheTargetAcceptingDrops  - Obviously, yes morons.
         flavorMap                  - I use the default one.
         */
		labelCurpDropHereMessage = palantirContext.getUiBuilders().labelBuilder()
				.text("Arrastra aquí el archivo PDF...")
				.border(BorderFactory.createDashedBorder(null, 1, 5, 1, true))
				.alignCenter()
				.fontSize(30.0f)
				.build();
		
        droptarget = new DropTarget(labelCurpDropHereMessage, DnDConstants.ACTION_COPY_OR_MOVE, this, Boolean.TRUE, null);
		
        panelDropArea = palantirContext.getUiBuilders().panelBuilder().build();
        panelDropArea.setBorder(BorderFactory.createEtchedBorder());
        BorderLayout layoutPanelDropArea = new BorderLayout(5,5);
        panelDropArea.setLayout(layoutPanelDropArea);
        
        //Adding blank padding components to allow the container to show their specified gap
        panelDropArea.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.NORTH);
        panelDropArea.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.SOUTH);
        panelDropArea.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.WEST);
        panelDropArea.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.EAST);
        panelDropArea.add(labelCurpDropHereMessage, BorderLayout.CENTER);
        
        this.add(panelDropArea, "0,1,1,1");
        
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("Object: " + evt.getSource().toString() + ", has fired this property: " + evt.getPropertyName());
		if( evt.getPropertyName().equalsIgnoreCase(IMAGE_IN_CLIPBOARD)){
			setCurpImage( (Image) evt.getNewValue() );
    		setInformationMap(InfoReader.getTextFromPDF(getPdfFile()));
    		firePropertyChange(SHOW_CURP_DATA, false, true);
		}
	}
	
	public void dragEnter(DropTargetDragEvent arg0) {
		labelCurpDropHereMessage.setText(DROP_FILE_HERE);
	}

	public void dragExit(DropTargetEvent arg0) {
		labelCurpDropHereMessage.setText(DRAG_FILE_HERE);
	}

	public void dragOver(DropTargetDragEvent arg0) {
	}

	public void drop(DropTargetDropEvent dtde) {
		
		try {
			
            // Accept CURP *.pdf file dropped into the component configured...
            dtde.acceptDrop(dtde.getDropAction());

            Transferable transferable = dtde.getTransferable();
            
            if( !transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ) {
            	
            } else {
            	
				// Getting a list of the dropped files
				@SuppressWarnings("unchecked")
				List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
									
	            if( droppedFiles.size() > 1 ) {
	            	JOptionPane.showMessageDialog(null, //getDisplayComponent(), 
	            			"Ha arrastrado más de un archivo. Arrastre solamente un archivo a la vez.", 
	            			"Información", 
	            			JOptionPane.INFORMATION_MESSAGE);
	            	labelCurpDropHereMessage.setText(DROP_FILE_HERE);
	            } else if ( droppedFiles.size() > 0 && droppedFiles.get(0).getName().endsWith(".pdf") ){
	            	            
		            // Setting the CURP *.pdf file.
		            setPdfFile( droppedFiles.get(0) );
	
		            if( getPdfFile() != null) {
			            labelCurpDropHereMessage.setText(PROCESSING_DROPPED_FILE);
			            
			            /*	Using a system-dependent mechanism, we get an screenshot of the CURP *.pdf file,
			            	such a way we can save an image file of it.           
			            */
			            SwingWorker<Image, String> worker = new SwingWorker<Image, String>() {
			    			
				            @Override
				            protected void done() {
				    				try {
				    					Image image = get();
				    			    	if( image == null ){
				    			    		
				    			    	} else {
				    			    		firePropertyChange(IMAGE_IN_CLIPBOARD, null, image);
				    			    	}
									} catch (InterruptedException | ExecutionException e) {
										Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, null, e);
									}
				            }				    	            	
			            	            	
			                @Override
			                protected Image doInBackground() throws Exception {
			                	InfoReader.putCURPImageInClipboard( getPdfFile(), InfoReader.MANUAL );
	    	                    return (InfoReader.isImageInClipboard())? InfoReader.getImageFromClipboard() : null;
			                }
			            };
			            worker.addPropertyChangeListener(this);
			            worker.execute();	
		            }         
	            }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	private File getPdfFile() {
		return this.pdfFile;
	}

	private void setPdfFile(File file) {
		this.pdfFile = file;
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
	}
		
    public void setCurpImage(Image curp) {
        if (curp == null) {
        	JOptionPane.showMessageDialog(null/*getDisplayComponent()*/, "No se pudo obtener la imagen!");
        } else {
            this.curpImage = curp;
        }
    }
  
    public Image getCurpImage() {
    	return this.curpImage;
    }
    
    public void setInformationMap(String data) {
    	
    	//Initializing/clearing information map
    	if( infoMap == null ){
    		infoMap = new LinkedHashMap<>();
    	} else {
    		infoMap.clear();
    	}
    	
        // Once we have read the text, we create a list of raw data, spliting the text by the newLine char.
        List<String> dataList = Arrays.asList(data.split("\n"));
    	
        for (String token : dataList) {
            String[] info;

            if (token.contains("ENTIDAD:")) {
                info = token.split("ENTIDAD:");
                infoMap.put("CLAVE", info[0].trim());
                infoMap.put("ENTIDAD", info[1].trim());
            } else if (token.contains("MUNICIPIO:")) {
                info = token.split("MUNICIPIO:");
                infoMap.put("MUNICIPIO", info[1].trim());
            } else if (token.contains("AÑO DE REGISTRO:")) {
                info = token.split("AÑO DE REGISTRO:");
                infoMap.put("NOMBRE_A", info[0].trim());
                infoMap.put("AÑO DE REGISTRO", info[1].trim());
            } else if (token.contains("NUMERO DE LIBRO:")) {
                info = token.split("NUMERO DE LIBRO:");
                infoMap.put("NOMBRE_B", info[0].trim());
                infoMap.put("NUMERO DE LIBRO", info[1].trim());
            } else if (token.contains("NUMERO DE ACTA:")) {
                info = token.split("NUMERO DE ACTA:");
                infoMap.put("NOMBRE_C", info[0].trim());
                infoMap.put("NUMERO DE ACTA:", info[1].trim());
            } else if (token.contains("NUMERO DE FOJA:")) {
                info = token.split("NUMERO DE FOJA:");
                infoMap.put("NUMERO DE FOJA", info[0].trim());
            } else if (token.contains("CRIP:")) {
                info = token.split("CRIP:");
                infoMap.put("FECHA DE INSCRIPCION", info[0].trim());
                infoMap.put("CRIP", info[1].trim());
            } else if (token.contains("Autenticidad:")) {
                info = token.split("Autenticidad:");
                infoMap.put("AUTENTICIDAD", info[1].trim());
            } else {
                Pattern pattern = Pattern.compile("^(\\s+\\d+)$");
                Matcher matcher = pattern.matcher(token);
                while (matcher.find()) {
                    String result = matcher.group().trim();
                    infoMap.put("FOLIO", result);
                }
            }
        }
    }	
    
    public Map<String, String> getInformationMap() {
    	return this.infoMap;
    }

	public void setDropAreaText(String readyToReceiveDrops) {
		labelCurpDropHereMessage.setText(DRAG_FILE_HERE);
	}

}
