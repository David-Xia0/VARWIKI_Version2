package application;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class MainMenu{


	private Button _listButton;
	private Button _createButton;
	private Stage _primaryStage;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 

	/**
	 * 
	 * @param primaryStage
	 */
	MainMenu(Stage primaryStage) {
		_primaryStage=primaryStage;
	}

	/**
	 * creates the menu scene. 
	 */
	public void displayMenu(){

		_primaryStage.setTitle("Create a Creation");
		_listButton = new Button("List Creations");
		_createButton = new Button("Create Creation");
		BorderPane pane = new BorderPane();

		_listButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				ListCreations list = new ListCreations(_primaryStage);
				list.listCreations();
			}
		});

		_createButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				CreateCreation create = new CreateCreation(_primaryStage);
				create.searchTerm();
			}
		});

		Text text = new Text();
		text.setText("Welcome To VARWIKI, please select an option below.");
		text.setFont(Font.font ("Verdana", 20));
		text.wrappingWidthProperty().bind(pane.widthProperty());
		pane.setTop(text);

		VBox cPane = new VBox( _listButton, _createButton);
		cPane.setAlignment(Pos.CENTER);
		cPane.setSpacing(30);
		pane.setCenter(cPane);

		Scene scene = new Scene(pane,400,400);
		_primaryStage.setScene(scene);
		_primaryStage.show();
	}

	/**
	 * Make sure file system is set up so not errors are encountered later on
	 */
	public void initiateFileSystem() {
		_team.submit(new RunBash("rm -r ./temp"));
		_team.submit(new RunBash("mkdir ./VideoCreations ./temp"));

	}

}
