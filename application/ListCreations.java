package application;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 
 * @author davidxiao
 *
 */
public class ListCreations {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	Stage _primaryStage;
	private List<String> _creations = new ArrayList<String>();

	ListCreations(Stage primaryStage){
		_primaryStage=primaryStage;
	}

	/**
	 * 
	 */
	public void listCreations() {
		BorderPane pane = new BorderPane(); 

		Text titleText = new Text("Creations:");
		titleText.setFont(Font.font ("Verdana", 20));
		pane.setTop(titleText);

		ScrollPane scrollPane = new ScrollPane();
		VBox videoCreations = new VBox();

		//Obtain all Video is ./VideoCreations directory
		RunBash bash = new RunBash("List=`ls ./VideoCreations` ; List=${List//.???/} ; printf \"${List// /.\\\\n}\\n\"");
		_team.submit(bash);
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					_creations = bash.get();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(_creations.get(0).isEmpty()) {
					Text noCreations = new Text("No Current Creations");
					videoCreations.getChildren().add(noCreations);
				}else {
					for(String video:_creations) {
						new VideoBar(video,videoCreations);
					}
				}
			}
		});

		//Display all VideoCreations in the centre of the scene/pane
		videoCreations.setSpacing(5);
		scrollPane.setContent(videoCreations);
		pane.setCenter(scrollPane);

		Button returnButton = new Button("return to menu");
		returnButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				MainMenu menu = new MainMenu(_primaryStage);
				menu.displayMenu();

			}
		});

		pane.setBottom(returnButton);
		_primaryStage.setScene(new Scene(pane,400,400));
		_primaryStage.show();
	}

}
