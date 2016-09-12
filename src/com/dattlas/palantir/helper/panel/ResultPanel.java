package com.dattlas.palantir.helper.panel;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.dattlas.palantir.helper.util.Constants;
import com.palantir.api.dataevent.PalantirDataEventType;
import com.palantir.api.ui.TableLayouts;
import com.palantir.api.workspace.PalantirConnection;
import com.palantir.api.workspace.PalantirWorkspaceContext;
import com.palantir.services.ptobject.DataSourceRecord;
import com.palantir.services.ptobject.PTObject.SetterStyle;
import com.palantir.services.ptobject.PTObjectContainer;
import com.palantir.services.ptobject.PTObjectContainers;
import com.palantir.services.ptobject.PTObjectType;
import com.palantir.services.ptobject.Property;
import com.palantir.services.ptobject.PropertyType;
import com.palantir.services.ptobject.Role;
import com.palantir.util.Locatables;

public class ResultPanel extends JPanel {

	private static final long serialVersionUID = 8680334417305558072L;
	
	public static final String CLEAR_CURP_CONTENTS = "clear curp contents";

    private JTextField fieldAnioRegistro;
    private JTextField fieldClave;
    private JTextField fieldCrip;
    private JTextField fieldEntidad;
    private JTextField fieldFechaInscripcion;
    private JTextField fieldFolio;
    private JTextField fieldMunicipio;
    private JTextField fieldNombreCiudadano;
    private JTextField fieldNumeroActa;
    private JTextField fieldNumeroFoja;
    private JTextField fieldNumeroLibro;
    private JTextField fieldNumeroTomo;

    private JTextArea textAreaAutenticidad;
    
    private JLabel labelCurpImage;
	private JLabel labelFechaInscripcion;
	private JLabel labelFolio;
	private JLabel labelNombreCiudadano;
	private JLabel labelClave;
	private JLabel labelEntidad;
	private JLabel labelMunicipio;
	private JLabel labelAnioRegistro;
	private JLabel labelNumeroLibro;
	private JLabel labelNumeroActa;
	private JLabel labelNumeroFoja;
	private JLabel labelNumeroTomo;
	private JLabel labelCrip;
	private JLabel labelAutenticidad;	
	
	private Image curpImage;

	private JPanel panelCurpImagePreview;
	private JPanel panelDatosCurp;
	private JPanel panelInformacion;
	private JPanel panelDatosActaNacimiento;	
	private JPanel panelAutenticidad;
	private JPanel panelOptions;	
	
	private JTabbedPane tabbedPaneInformacion;
	
	private JButton buttonEraseCurpData;
	private JButton buttonSendDataToGraph;
		
	private PalantirWorkspaceContext palantirContext;

    
	public ResultPanel(PalantirWorkspaceContext palantirContext) {
		
		this.palantirContext = palantirContext;
		
		TableLayout dataLayout = TableLayouts.create("10,p,5,f,10", "10,p,5,f,10", 5, 5);
		setLayout(dataLayout);
		
		initFields();
		
		this.add(panelCurpImagePreview, 	"1,1");
		this.add(panelOptions, 				"3,1");
		this.add(panelInformacion, 			"1,3,3,3");
	}
	
	public void setCurpImage(Image curpImage){
		this.curpImage = curpImage;
		setCurpImagePreview();
	}
	
	private void setCurpImagePreview(){
		if( curpImage != null ) {
			Dimension labelCurpImageSize = labelCurpImage.getPreferredSize();
			labelCurpImage.setIcon( new ImageIcon( curpImage.getScaledInstance(labelCurpImageSize.width, labelCurpImageSize.height, Image.SCALE_SMOOTH) ));
		}
	}
	
    public void setData(Map<String, String> data) {
        String nombreCiudadano = "";

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "ENTIDAD":
                    fieldEntidad.setText(value);
                    break;
                case "CLAVE":
                    fieldClave.setText(value);
                    break;
                case "MUNICIPIO":
                    fieldMunicipio.setText(value);
                    break;
                case "AÑO DE REGISTRO":
                    fieldAnioRegistro.setText(value);
                    break;
                case "NOMBRE_A":
                    nombreCiudadano += value + " ";
                    break;
                case "NOMBRE_B":
                    nombreCiudadano += value + " ";
                    break;
                case "NOMBRE_C":
                    nombreCiudadano += value;
                    fieldNombreCiudadano.setText(nombreCiudadano);
                    break;
                case "NUMERO DE LIBRO":
                    fieldNumeroLibro.setText(value);
                    break;
                case "NUMERO DE ACTA:":
                    fieldNumeroActa.setText(value);
                    break;
                case "NUMERO DE FOJA":
                    fieldNumeroFoja.setText(value);
                    break;
                case "FECHA DE INSCRIPCION":
                    fieldFechaInscripcion.setText(value);
                    break;
                case "CRIP":
                    fieldCrip.setText(value);
                    break;
                case "FOLIO":
                    fieldFolio.setText(value);
                    break;
                case "AUTENTICIDAD":
                    textAreaAutenticidad.setText(value);
                    break;
            }

        }
    }	
	
	private void initFields() {
		
		/////////////////////////////////////////////////////////////////////////////////
		/*
		* Curp image preview panel configucation.
		*/
		/////////////////////////////////////////////////////////////////////////////////
		
		labelCurpImage			= palantirContext.getUiBuilders().labelBuilder()
		.alignCenter()
		.border(BorderFactory.createEtchedBorder())
		.build();
		labelCurpImage.setPreferredSize(new Dimension(285,219));
		
		panelCurpImagePreview = palantirContext.getUiBuilders().panelBuilder()
		.layout(TableLayouts.create("5,p,5", "5,p,5"))
		.border(BorderFactory.createTitledBorder(" Imagen del CURP "))
		.add(labelCurpImage, "1,1")
		.build();

		/////////////////////////////////////////////////////////////////////////////////
		/*
		*  Configuring option buttons.
		*/
	    /////////////////////////////////////////////////////////////////////////////////
	    
		buttonSendDataToGraph		= palantirContext.getUiBuilders().buttonBuilder()
				.actionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						buttonSendDataToGraphActionPerformed(evt);
					}
				})
				.text("Send CURP to Graph... ")
				.build();
		
		buttonEraseCurpData			= palantirContext.getUiBuilders().buttonBuilder()
				.actionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						buttonEraseCurpDataActionPerformed(evt);
					}
				})
				.text("Clear CURP information")
				.build();
		
	    panelOptions			= palantirContext.getUiBuilders().panelBuilder()
	    		.border(BorderFactory.createTitledBorder(" Options "))
	    		.layout(TableLayouts.create("10,f,10", "5,m,10,m,m,5"))
	    		.add(buttonSendDataToGraph, "1,1")
	    		.add(buttonEraseCurpData, "1,3")
	    		.build();
	    
		/////////////////////////////////////////////////////////////////////////////////
		/*
		 *	Datos del CURP
		 */
		/////////////////////////////////////////////////////////////////////////////////
		
		Dimension dimensionLabel = new Dimension(135, 20);	// This is the uniform size the labels will have.
		
	    fieldNombreCiudadano 	= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();    
	    fieldClave 				= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldFechaInscripcion 	= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldFolio 				= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    	    
	    labelNombreCiudadano	= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Nombre del ciudadano")
	    		.build();
	    labelClave				= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Clave")
	    		.build();
	    labelFolio				= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Folio")
	    		.build();
	    labelFechaInscripcion	= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Fecha de Inscripción")
	    		.build();
	   
	    fieldNombreCiudadano.setMinimumSize		( new Dimension(312, 20)	);
	    fieldClave.setMinimumSize				( new Dimension(280, 20)	);
	    fieldFechaInscripcion.setMinimumSize	( new Dimension(124, 20)	);
	    fieldFolio.setMinimumSize				( new Dimension(124, 20)	);
	    labelNombreCiudadano.setMinimumSize		( dimensionLabel			);
	    labelClave.setMinimumSize				( dimensionLabel			);
	    labelFolio.setMinimumSize				( dimensionLabel			);
		labelFechaInscripcion.setMinimumSize	( dimensionLabel			);
		
		fieldNombreCiudadano.setEditable	(false);
		fieldClave.setEditable				(false);
		fieldFechaInscripcion.setEditable	(false);
		fieldFolio.setEditable				(false);
		
	    /////////////////////////////////////////////////////////////////////////////////
	    /*
	     *	Datos del Acta de nacimiento 
	     */
	    /////////////////////////////////////////////////////////////////////////////////
	    
	    fieldEntidad 			= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldMunicipio 			= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
		fieldAnioRegistro 		= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldNumeroLibro 		= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldNumeroActa 		= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldNumeroFoja 		= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldNumeroTomo 		= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    fieldCrip 				= palantirContext.getUiBuilders().textFieldBuilder()
	    		.text("")
	    		.build();
	    
	    labelEntidad			= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Entidad")
	    		.build();
	    labelMunicipio			= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Municipio")
	    		.build();
	    labelAnioRegistro		= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Año de Registro")
	    		.build();
	    labelNumeroLibro		= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Número de Libro")
	    		.build();
	    labelNumeroActa			= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Número de Acta")
	    		.build();
	    labelNumeroFoja			= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Número de Foja")
	    		.build();
	    labelNumeroTomo			= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Número de Tomo")
	    		.build();
	    labelCrip				= palantirContext.getUiBuilders().labelBuilder()
	    		.text("CRIP")
	    		.build();
	    
	    fieldEntidad.setMinimumSize			(	new Dimension(250, 20)	);
	    fieldMunicipio.setMinimumSize		(	new Dimension(250, 20)	);
	    fieldAnioRegistro.setMinimumSize	(	new Dimension(80, 20)	);
	    fieldNumeroLibro.setMinimumSize		(	new Dimension(100, 20)	);
	    fieldNumeroActa.setMinimumSize		(	new Dimension(100, 20)	);
	    fieldNumeroFoja.setMinimumSize		(	new Dimension(100, 20)	);
	    fieldNumeroTomo.setMinimumSize		(	new Dimension(100, 20)	);
	    fieldCrip.setMinimumSize			(	new Dimension(170, 20)	);
	    
	    labelEntidad.setMinimumSize			(	dimensionLabel	);
	    labelMunicipio.setMinimumSize		(	dimensionLabel	);
	    labelAnioRegistro.setMinimumSize	(	dimensionLabel	);
	    labelNumeroLibro.setMinimumSize		(	dimensionLabel	);
	    labelNumeroActa.setMinimumSize		(	dimensionLabel	);
	    labelNumeroFoja.setMinimumSize		(	dimensionLabel	);
	    labelNumeroTomo.setMinimumSize		(	dimensionLabel	);
	    labelCrip.setMinimumSize			(	dimensionLabel	);
	    
	    fieldEntidad.setEditable		(false);
	    fieldMunicipio.setEditable		(false);
	    fieldAnioRegistro.setEditable	(false);
	    fieldNumeroLibro.setEditable	(false);
	    fieldNumeroActa.setEditable		(false);
	    fieldNumeroFoja.setEditable		(false);
	    fieldNumeroTomo.setEditable		(false);
	    fieldCrip.setEditable			(false);
	    
	    /////////////////////////////////////////////////////////////////////////////////
	    /*
	     *	Autenticidad
	     */
	    /////////////////////////////////////////////////////////////////////////////////
	    
	    textAreaAutenticidad 			= palantirContext.getUiBuilders().textAreaBuilder()
	    		.text("")
	    		.build();
	    
	    labelAutenticidad				= palantirContext.getUiBuilders().labelBuilder()
	    		.text("Autenticidad")
	    		.build();
	    
	    textAreaAutenticidad.setRows(2);
	    textAreaAutenticidad.setLineWrap(true);
	    textAreaAutenticidad.setWrapStyleWord(true);
	    textAreaAutenticidad.setEditable(false);
	    
	    labelAutenticidad.setMinimumSize	(	dimensionLabel	);
	    
	    /////////////////////////////////////////////////////////////////////////////////
	    /*
	     *  Configuring panel that will display the CURP information in a proper way.
	     */
	    /////////////////////////////////////////////////////////////////////////////////
	    
	    panelInformacion 		= palantirContext.getUiBuilders().panelBuilder()
	    		.layout(TableLayouts.create("5,f,5", "5,f,5"))
	    		.border(BorderFactory.createTitledBorder(" Información del CURP "))
	    		.build(); 
	    
	    tabbedPaneInformacion = new JTabbedPane();
	    panelInformacion.add(tabbedPaneInformacion, "1,1");
	    
	    panelDatosCurp 			= palantirContext.getUiBuilders().panelBuilder()
	    		.layout(TableLayouts.create("5,m,10,f,5", "5,m,5,m,5,m,5,m,f"))
	    		.build();
	    
	    panelDatosCurp.add( labelNombreCiudadano, 	"1,1");
	    panelDatosCurp.add( fieldNombreCiudadano, 	"3,1");
	    panelDatosCurp.add( labelClave, 			"1,3");
	    panelDatosCurp.add( fieldClave, 			"3,3");
	    panelDatosCurp.add( labelFechaInscripcion, 	"1,5");
	    panelDatosCurp.add( fieldFechaInscripcion, 	"3,5");
	    panelDatosCurp.add( labelFolio, 			"1,7");
	    panelDatosCurp.add( fieldFolio, 			"3,7");
	    
	    tabbedPaneInformacion.addTab("Datos del CURP", panelDatosCurp);
	    
	    panelDatosActaNacimiento = palantirContext.getUiBuilders().panelBuilder()
	    		.layout(TableLayouts.create("5,m,10,f,5", "5,m,5,m,5,m,5,m,5,m,5,m,5,m,5,m,f"))
	    		.build();

	    panelDatosActaNacimiento.add( labelEntidad, 		"1,1");
	    panelDatosActaNacimiento.add( fieldEntidad, 		"3,1");
	    panelDatosActaNacimiento.add( labelMunicipio, 		"1,3");
	    panelDatosActaNacimiento.add( fieldMunicipio, 		"3,3");
	    panelDatosActaNacimiento.add( labelAnioRegistro, 	"1,5");
	    panelDatosActaNacimiento.add( fieldAnioRegistro, 	"3,5");
	    panelDatosActaNacimiento.add( labelNumeroLibro, 	"1,7");
	    panelDatosActaNacimiento.add( fieldNumeroLibro, 	"3,7");
	    panelDatosActaNacimiento.add( labelNumeroActa, 		"1,9");
	    panelDatosActaNacimiento.add( fieldNumeroActa, 		"3,9");
	    panelDatosActaNacimiento.add( labelNumeroFoja, 		"1,11");
	    panelDatosActaNacimiento.add( fieldNumeroFoja, 		"3,11");
	    panelDatosActaNacimiento.add( labelNumeroTomo, 		"1,13");
	    panelDatosActaNacimiento.add( fieldNumeroTomo, 		"3,13");
	    panelDatosActaNacimiento.add( labelCrip, 			"1,15");
	    panelDatosActaNacimiento.add( fieldCrip, 			"3,15");
	    
	    tabbedPaneInformacion.addTab("Datos del Acta de Nacimiento", panelDatosActaNacimiento);
	    
	    panelAutenticidad = palantirContext.getUiBuilders().panelBuilder()
	    		.layout(TableLayouts.create("5,m,10,f,5", "5,m,5,m,f"))
	    		.build();
	    
	    panelAutenticidad.add( labelAutenticidad, 		"1,1");
	    panelAutenticidad.add( textAreaAutenticidad, 	"3,1,3,3");
	    
	    tabbedPaneInformacion.addTab("Autenticidad", panelAutenticidad);
	}

	private void buttonSendDataToGraphActionPerformed(ActionEvent evt) {
		System.out.println("Sending to graph...");
		
		final PalantirConnection connection = palantirContext.getPalantirConnection();
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				
				//Creating data source records...
				DataSourceRecord dsr = connection.getDsrFactory().createManuallyEnteredDsr();
				Collection<DataSourceRecord> dsrs = Collections.singleton(dsr);
				
				//Creating CURP object...
				PTObjectType ptot = palantirContext.getOntology().getObjectTypeByUri(Constants.URI_OBJECT_CURP);
				PTObjectContainer ptoc = PTObjectContainers.createBlankObject(connection, ptot);
				
				//Getting the data for the object/properties...
				String dataNombre 					= fieldNombreCiudadano	.getText();
				String dataClave 					= fieldClave			.getText();
				String dataFechaInscripcionString 	= fieldFechaInscripcion	.getText();
				String dataFolio					= fieldFolio.getText();
				
				//Setting the CURP Object title/label...
				ptoc.setTitle(dataClave, connection, dsrs, SetterStyle.KEEP_OTHERS, Property.MANUALLY_SET_PRIORITY);
					
				try {
					//Setting properties..
					PropertyType nombre 			= palantirContext.getOntology().getPropertyTypeByUri(Constants.URI_PROPERTY_CITIZEN_NAME);
					PropertyType clave 				= palantirContext.getOntology().getPropertyTypeByUri(Constants.URI_PROPERTY_CURP);
					PropertyType fechaInscripcion 	= palantirContext.getOntology().getPropertyTypeByUri(Constants.URI_PROPERTY_REGISTRATION_DATE);
					PropertyType folio 				= palantirContext.getOntology().getPropertyTypeByUri(Constants.URI_PROPERTY_FOIL_NUMBER);		
					
					Property propertyNombre 			= Property.attemptToCreate(connection, nombre, dataNombre, Role.NONE);
					Property propertyClave 				= Property.attemptToCreate(connection, clave, dataClave, Role.NONE);
					Property propertyFechaInscripcion 	= Property.attemptToCreate(connection, fechaInscripcion, dataFechaInscripcionString, Role.NONE);
					Property propertyFolio 				= Property.attemptToCreate(connection, folio, dataFolio, Role.NONE);
					
					propertyNombre.addDataSourceRecord(connection.getDsrFactory().copyDsr(dsr));
					propertyClave.addDataSourceRecord(connection.getDsrFactory().copyDsr(dsr));
					propertyFechaInscripcion.addDataSourceRecord(connection.getDsrFactory().copyDsr(dsr));
					propertyFolio.addDataSourceRecord(connection.getDsrFactory().copyDsr(dsr));
					
					ptoc.addProperty(propertyNombre);
					ptoc.addProperty(propertyClave);
					ptoc.addProperty(propertyFechaInscripcion);
					ptoc.addProperty(propertyFolio);
					
					List<PTObjectContainer> ptocs = new ArrayList<PTObjectContainer>();
					ptocs.add(ptoc);
									
					//Storing this object in the current investigative base realm...
					connection.storeObjects(ptocs, PalantirDataEventType.DATA);
					
					//Adding this object to the Palantir Graph Application
					palantirContext.getGraph().addObjects(Locatables.getLocatorList(ptocs));
					
				} catch( Exception e ){
					Logger.getLogger(ResultPanel.class.getName()).log(Level.WARNING, null, e);
				}
								
				return null;
			}
		};
		
		palantirContext.getMonitoredExecutorService().execute(worker);
	}

	private void buttonEraseCurpDataActionPerformed(ActionEvent evt) {
		firePropertyChange(CLEAR_CURP_CONTENTS, false, true);
	}

	
	public String getClave() {
		return this.fieldClave.getText();
	}
	
	
}
