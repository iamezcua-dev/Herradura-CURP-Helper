package com.dattlas.palantir.helper.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author iamezcua
 */
public class InfoReader {

    public static final int MANUAL 		= 1;
    public static final int AUTOMATIC 	= 2;
	
    /**
     * @param file
     * @return
     */
    public static String getTextFromPDF(File pdfFile) {
        String data = null;

        if (!pdfFile.exists()) {
            System.out.println("File not found!");
        } else {
            String extension = pdfFile.getName().substring(pdfFile.getName().lastIndexOf(".")).toLowerCase();

            if (extension.toLowerCase().equals(".pdf")) {
	        	try {
	                RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
	                PDFParser pdfParser = new PDFParser(raf);
	                pdfParser.parse();
	                COSDocument cosDocument = pdfParser.getDocument(); 			// In-memory representation of the PDF File.
	                PDFTextStripper pdfTextStripper = new PDFTextStripper(); 	// Stripping out the text of the PDF file.
	                PDDocument pdDocument = new PDDocument(cosDocument); 		// Wrapper of the in-memory representation of the document.
	                int numberOfPages = pdDocument.getNumberOfPages();
	                pdfTextStripper.setStartPage(1);
	                pdfTextStripper.setEndPage(numberOfPages);
	
	                data = pdfTextStripper.getText(pdDocument);
	
	                cosDocument.close();
	
	            } catch (IOException ex) {
	            	data = null;
	                Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, null, ex);
	            }
            } else {
            	data = null;
            	Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, "This file type cannot be rendered ( *." + extension + " file ).", (Throwable) null);
            }
        }
        
        return data;
    }

    /**
     * Help Palantir Workspace (R) analysts to take a snapshot of a mexican
     * government Clave Única de Registro de Población (CURP) document.
     *
     * This method, opens a PDF viewer and starts a snipping tool that allows
     * the user to select the snapshot area.
     *
     * In order to know what PDF viewing software must be used, this method
     * reads an environment variable called <code>PDF_READER</code>, so it's up
     * to the System Administrator to properly configure it, before using this
     * method.
     *
     * @param pdfFile The CURP PDF file to read from.
     * @param mode AUTOMATIC or MANUAL mode
     * @return The CURP snapshot taken. <code>null</code>, if the operation failed.
     */
    public static void putCURPImageInClipboard(final File pdfFile, Integer mode) {
    	            
    	switch( mode ) {
    	
    	case MANUAL:

	        System.out.println("Thread started: " + Thread.currentThread().getName());
	        
	        InfoReader.clearClipboard();
	
	        boolean isPDFViewerRunning = false;
	        boolean isSnippingtoolRunning = false;
	        boolean isImageInClipboard = false;
	
	        try {
	            /**
	             * 1. Abrir el PDF con el programa especificado en
	             * la variable del sistema PDF_READER.
	             *
	             * 2. Esperar 3 segundos hasta que termine de
	             * iniciar.
	             *
	             * 3. Abrir la herramienta para toma de capturas de
	             * pantalla 'snippingtool'.
	             */
	
	            Process processOpenCURP = Runtime.getRuntime().exec("cmd /c start " + System.getenv("PDF_READER") + " \"" + pdfFile.getAbsolutePath() + "\"");
	            processOpenCURP.waitFor();
	
	            try {
	                Thread.sleep(Integer.parseInt(System.getenv("SNAP_DELAY")));
	            } catch (NumberFormatException e) {
	                Logger.getLogger(InfoReader.class.getName()).log(
	                        Level.WARNING,
	                        "[ERR] The number from the SNAP_DELAY system variable is not correctly set. Defaulting to 2000 ms.",
	                        e);
	                Thread.sleep(2000);
	            }
	
	            Process processOpenSnippingtool = Runtime.getRuntime().exec("cmd /c start snippingtool");
	            processOpenSnippingtool.waitFor();
	
	            while (!isPDFViewerRunning) {
	                isPDFViewerRunning = InfoReader.isRunning(System.getenv("PDF_READER"));
	            }
	
	            System.out.println("PDF Reader running? " + isPDFViewerRunning);
	
	            while (!isSnippingtoolRunning) {
	                isSnippingtoolRunning = InfoReader.isRunning("snippingtool");
	            }
	
	            System.out.println("Snippingtool running? " + isSnippingtoolRunning);
	
	            while (isPDFViewerRunning && isSnippingtoolRunning && !isImageInClipboard) {
	                isPDFViewerRunning = InfoReader.isRunning(System.getenv("PDF_READER"));
	                isSnippingtoolRunning = InfoReader.isRunning("snippingtool");
	                isImageInClipboard = InfoReader.isImageInClipboard();
	            }
	
	            String message = "antes de tomar la imagen del CURP. Sin embargo, se detectó una imagen en el portapapeles. Se añade al panel...";
	            if (!isPDFViewerRunning) {
	                System.out.println("Se cerró el Visor de PDF's " + ((isImageInClipboard) ? message : ""));
	                System.out.println("Cerrando también el capturador de imágenes...");
	            }
	
	            if (!isSnippingtoolRunning) {
	                System.out.println("Se cerró el capturador de imágenes " + ((isImageInClipboard) ? message : ""));
	                System.out.println("Cerrando también el Visor de PDF's...");
	            }
	
	            Runtime.getRuntime().exec("cmd /c tskill " + System.getenv("PDF_READER"));
	            Runtime.getRuntime().exec("cmd /c tskill snippingtool");
	
	            if (isImageInClipboard) {
	                System.out.println("Il y a une image dans le presse-papier détectée...");
	            } else {
	                System.out.println("Il n'y a pas une image dans le presse-papier...");
	            }
	        } catch (InterruptedException | IOException | NullPointerException ex) {
	            Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, null, ex);
	        }
	
	        System.out.println("Thread end: " + Thread.currentThread().getName());
            
            break;
            
    	case AUTOMATIC:
    		break;
    	}
    	
    }

    /**
     * Takes the system clipboard contents and constructs an
     * <code>Image</code>object from it.
     *
     * @return The <code>Image</code> object created using the data from the
     * clipboard. <code>null</code>, if the operation fails.
     */
    public static Image getImageFromClipboard() {
        Image image = null;

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        try {
            if (contents == null) {
                System.err.println("[ERR] Clipboard content is 'null'. Image object couldn't be created.");
            } 
            else if (!contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                System.err.println("[ERR] Cannot render clipboard contents as image. Image object couldn't be created.");
            } 
            else {
                image = (Image) contents.getTransferData(DataFlavor.imageFlavor);
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }
	
    /**
     * Sets the system clipboard contents to <code>null</code>.
     */
    public static void clearClipboard() {
    	System.out.println("Clearing clipboard...");
		try {
//			// This code doesn't work (and I don't really understand why.)
//			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//	        systemClipboard.setContents( new StringSelection(null) , (ClipboardOwner) null);
			Process p = Runtime.getRuntime().exec("cmd /c echo. | clip");
			p.waitFor();
	        System.out.println("Clipboard cleared...");
		} catch (InterruptedException | IOException e) {
			Logger.getLogger(InfoReader.class.getName()).log(Level.WARNING, null, e);
		} 
    }

    /**
     * Checks whether an image object can be taken from the system clipboard in
     * this moment.
     *
     * @return <code>true</code>, if the system clipboard content is compatible
     * with <code>Dataflavor.imageFlavor</code>. <code>false</code>, therwise
     */
    public static Boolean isImageInClipboard() {
    	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//    	System.out.println("----------------------------------------------------");
//    	for( DataFlavor dataFlavor : clipboard.getAvailableDataFlavors()){
//    		System.out.println(dataFlavor);
//    	}
        return clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor);
    }

    /**
     * Checks whether a program is currently running.
     *
     * @param programName The program name we want to verify.
     * @return Returns true if and only if the <code>programName</code> is
     * running
     */
    public static boolean isRunning(String programName) {

        Boolean isRunning = false;

        if (System.getenv("OS").toLowerCase().contains("windows".toLowerCase())) {
            try {
                Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\tasklist.exe");
                try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = streamReader.readLine()) != null) {
                        if (isRunning = line.toLowerCase().contains(programName.toLowerCase())) {
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, "Program not found: \"tasklist.exe\"", ex);
            }
        } else {
            System.err.println("Operating System not supported");
        }

        return isRunning;
    }
    
    public static String getClipboardText() {
        String text = "";

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable data = clipboard.getContents(null);

        if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                text = (String) data.getTransferData(DataFlavor.stringFlavor);

            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(InfoReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            text = "";
        }

        return text;
    }

}
