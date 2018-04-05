/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import accesoaBD.AccesoaBD;
import java.io.IOException;
import modelo.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 *  Suponemos que: <p>
 *          - Dos Alumnos son iguales si tienen el mismo DNI <p>
 *          - Dos Cursos son iguales si tienen el mismo título <p>
 * @author aleja
 */
public class PrincipalController implements Initializable {
    @FXML
    private ListView<Alumno> listAlumnos;
    @FXML
    private ListView<Curso> listCursos;
    @FXML
    private ListView<Alumno> listAlumnosDeCurso;
    @FXML
    private ImageView image;
    @FXML
    private Label labelNombre;
    @FXML
    private Label labelDNI;
    @FXML
    private Label labelEdad;
    @FXML
    private Label labelDireccion;
    @FXML
    private Label labelFechaAlta;
    @FXML
    private ComboBox<Curso> comboCursos;
    @FXML
    private Button buttonAlta;
    @FXML
    private Button buttonBaja;
    @FXML
    private Button buttonMatricular;
    @FXML
    private Button buttonDesmatricular;
    @FXML
    private Button buttonNewCurso;
    @FXML
    private Button buttonViewCurso;
    @FXML
    private Button buttonRemoveCurso;
    
    private final AccesoaBD acceso = new AccesoaBD();
    private final Alert exito = new Alert(AlertType.INFORMATION);
    private final ObservableList<Alumno> dataAlumnos = FXCollections.observableList(acceso.getAlumnos());
    private final ObservableList<Curso> dataCursos = FXCollections.observableList(acceso.getCursos());
    private final ArrayList<Matricula> dataMatriculas = (ArrayList<Matricula>) acceso.getMatriculas();
    private ObservableList<Alumno> dataAlumnosDeCurso = FXCollections.observableList(new ArrayList<>());
    private ObservableList<Curso> dataCursosDisponibles;
    
    //ListCells para Alumno y Curso
    class AlumnoListCell extends ListCell<Alumno> {
        @Override
        protected void updateItem(Alumno item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) { setText(null); }
            else { setText(item.getNombre()); }
        }
    }
    class CursoListCell extends ListCell<Curso> {
        @Override
        protected void updateItem(Curso item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) { setText(null); }
            else { setText(item.getTitulodelcurso()); }
        }
    }
    /**
     * "Curso Disponible" es aquel que: <p>
     *      - No tiene el mismo horario que otro curso en el que el alumno ya esta matriculado <p>
     *      - No tenga matriculados el numero maximo de alumnos <p>
     * @param c Curso del que se comprueba si esta disponible
     * @param a Alumno para el que debe estar disponible el curso
     * @return Si el curso esta disponible o no para el alumno
     */
    private boolean isAvailable(Curso c, Alumno a) { 
        List<Alumno> alumnosDeCurso = acceso.getAlumnosDeCurso(c);  
        /* Si el numero de alumnos del curso excede del maximo, no se puede matricular otro
        (si la lista de alumnos del curso es 'null', entonces no tiene alumnos) */
        if (alumnosDeCurso != null && alumnosDeCurso.size() == c.getNumeroMaximodeAlumnos()) return false;
        //Almacenamos los dias y la hora del curso
        List<Dias> diasImparte = c.getDiasimparte(); 
        LocalTime hora = c.getHora();
        //Buscamos los cursos en los que el alumno tiene matricula
        for (Matricula m : dataMatriculas) {
            // Si la matricula es del alumno, comprueba las horas del curso
            if (m.getAlumno().getDni().equals(a.getDni())) { 
                //Almacenamos la hora del curso en el que ya estaba matriculado
                LocalTime mHora = m.getCurso().getHora();
                // Si las horas coinciden, comprueba los dias
                if (mHora.equals(hora)) {
                    List<Dias> mDias = m.getCurso().getDiasimparte();
                    // Si alguno de los dias coincide, no esta disponible
                    for (Dias d : diasImparte) { if (mDias.contains(d)) return false; }
                }
            }
        }
        return true; //Si todo se ha cumplido correctamente, esta disponible
    }
    
    private ObservableList<Curso> getAvailableCursos(Alumno a) {
        List<Curso> res = new ArrayList<>(); 
        for (Curso c : dataCursos) {
            if (isAvailable(c, a)) { res.add(c); }
        }
        return FXCollections.observableList(res);
    }
    
    private void showAlumno(Alumno a) {
        image.setImage(a.getFoto());
        labelNombre.setText("Nombre: " + a.getNombre());
        labelDNI.setText("DNI: " + a.getDni());
        labelEdad.setText("Edad: " + a.getEdad());
        labelDireccion.setText("Dirección: " + a.getDireccion());
        labelFechaAlta.setText("Fecha de alta: " + a.getFechadealta());
        dataCursosDisponibles = getAvailableCursos(a);
        comboCursos.setItems(dataCursosDisponibles);
    }
    
    private void showAlumnosDeCurso(Curso c) {
        List<Alumno> alumnosDeCurso = acceso.getAlumnosDeCurso(c);
        if (alumnosDeCurso == null) { dataAlumnosDeCurso.clear(); }
        else {
            dataAlumnosDeCurso = FXCollections.observableList(alumnosDeCurso);
            listAlumnosDeCurso.setItems(dataAlumnosDeCurso);
        }
    }
    
    private void gotoDialogueCursos(Curso actCurso) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/proyecto/view/DialogueCursoView.fxml"));
            Parent root = (Parent) myLoader.load();
            DialogueCursoController dialogue = myLoader.<DialogueCursoController>getController();
            dialogue.init(stage, actCurso);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {}
    }
    //POR IMPLEMENTAR: TENER EN CUENTA LAS MATRICULAS ANTES DE ELIMINAR, Y AVISAR AL USUARIO EN CONSECUENCIA
    private void remove(Alumno a) {
        dataAlumnos.remove(a);
        acceso.salvar();
    }
    private void remove(Curso c) {
        dataCursos.remove(c);
        acceso.salvar();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        exito.setHeaderText(null);
        // Fijamos las CellFactory
        listAlumnos.setCellFactory(c -> new AlumnoListCell());
        listAlumnosDeCurso.setCellFactory(c -> new AlumnoListCell());
        listCursos.setCellFactory(c -> new CursoListCell());
        comboCursos.setButtonCell(new CursoListCell()); // En un comboBox, esta Cell hace referencia a la vista cuando esta seleccionado
        comboCursos.setCellFactory(c -> new CursoListCell()); // Y esta a cuando se ve en la lista del comboBox.
        
        //Llenamos las listas con sus respectivos datos
        listAlumnos.setItems(dataAlumnos);
        listCursos.setItems(dataCursos);
        
        // Hacemos que aquellos botones que dependan de que haya un elemento seleccionado en una lista solo esten disponibles cuando lo haya
        buttonMatricular.disableProperty().bind(
                Bindings.equal(-1,
                        comboCursos.getSelectionModel().selectedIndexProperty()));
        buttonDesmatricular.disableProperty().bind(
                Bindings.equal(-1,
                        listAlumnosDeCurso.getSelectionModel().selectedIndexProperty()));
        buttonBaja.disableProperty().bind(
                Bindings.equal(-1,
                        listAlumnos.getSelectionModel().selectedIndexProperty()));
        buttonViewCurso.disableProperty().bind(
                Bindings.equal(-1,
                        listCursos.getSelectionModel().selectedIndexProperty()));
        buttonRemoveCurso.disableProperty().bind(
                Bindings.equal(-1,
                        listCursos.getSelectionModel().selectedIndexProperty()));
        
        //Cada vez que se seleccione un alumno, mostramos sus datos
        listAlumnos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showAlumno(newValue));
        
        //Mostramos los alumnos matriculados en un curso al seleccionarlo
        listCursos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showAlumnosDeCurso(newValue));
        
        //Codigo para matricular a un alumno en un curso
        buttonMatricular.setOnAction((e) -> {
            Alumno a = listAlumnos.getSelectionModel().getSelectedItem();
            Curso c = comboCursos.getSelectionModel().getSelectedItem();
            dataMatriculas.add(new Matricula(LocalDate.now(), c, a));
            acceso.salvar();
            dataCursosDisponibles.remove(c);
            //Si el curso se encontraba seleccionado en la lista de cursos, añadimos al nuevo alumno
            Curso selC = listCursos.getSelectionModel().getSelectedItem();
            if (c.getTitulodelcurso().equals(selC.getTitulodelcurso())) dataAlumnosDeCurso.add(a);
            exito.setContentText("El alumno ha sido matriculado correctamente");
            exito.show();
        });
        
        //Codigo para desmatricular a un alumno de un curso
        buttonDesmatricular.setOnAction((e) -> {
            Alumno a = listAlumnosDeCurso.getSelectionModel().getSelectedItem();
            Curso c = listCursos.getSelectionModel().getSelectedItem();
            List<Matricula> l = acceso.getMatriculasDeCurso(c); //Obtenemos las matriculas del curso
            int count = 0; //Buscamos el indice en la lista de la matricula
            while (count < l.size() && !l.get(count).getAlumno().equals(a)) count++;
            if (count < l.size()) {
                dataMatriculas.remove(l.get(count));
                acceso.salvar();
                dataAlumnosDeCurso.remove(a);
                //Si el alumno esta seleccionado, añade el curso a la lista de cursos disponibles
                Alumno selA = listAlumnos.getSelectionModel().getSelectedItem();
                if (a.getDni().equals(selA.getDni())) dataCursosDisponibles.add(c);
                exito.setContentText("El alumno ha sido desmatriculado correctamente");
                exito.show();
            } else {
                new Alert(AlertType.ERROR, "Ha habido un error inesperado. Inténtelo de nuevo más tarde.").show();
            }
        });
        buttonNewCurso.setOnAction(e -> gotoDialogueCursos(null));
        buttonViewCurso.setOnAction(e -> gotoDialogueCursos(listCursos.getSelectionModel().getSelectedItem()));
        buttonBaja.setOnAction(e -> remove(listAlumnos.getSelectionModel().getSelectedItem()));
        buttonRemoveCurso.setOnAction(e -> remove(listCursos.getSelectionModel().getSelectedItem()));
    }
}
