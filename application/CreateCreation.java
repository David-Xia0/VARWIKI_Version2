package application;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 
 * @author davidxiao
 *
 */
public class CreateCreation {

	private String _term;
	private Stage _primaryStage;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private BorderPane _pane = new BorderPane();

	/**
	 * 
	 * @param primaryStage
	 */
	CreateCreation(Stage primaryStage){
		_primaryStage=primaryStage;
		_team.submit(new RunBash("rm -f ./temp/*"));
	}

	/**
	 * The user will be asked for a term to search in wikit
	 */
	public void searchTerm() {

		Text titleText = new Text("What Term Would you Like to Search?");
		titleText.setFont(Font.font ("Verdana", 20));
		titleText.wrappingWidthProperty().bind(_pane.widthProperty());
		_pane.setTop(titleText);

		//input for term user would like to wiki search
		TextField search = new TextField();
		VBox centreBox = new VBox(search);
		_pane.setCenter(centreBox);

		Button searchButton = new Button("Search");
		Button returnButton = new Button("return to menu");

		returnButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(_runningThread) {
					error("please wait for process to finish");
					return;
				}
				MainMenu menu = new MainMenu(_primaryStage);
				menu.displayMenu();

			}
		});

		searchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {		
				_term = search.getCharacters().toString();

				
				if(_term.isEmpty()) {
					error("please enter a term");
					return;
				}else if(_runningThread) {
					error("please wait for process to finish");
					return;
				}

				searchButton.setText("Searching...");

				//wiki search bash command is created and run on another thread
				RunBash command = new RunBash("wikit "+ _term);
				_team.submit(command);
				_runningThread = true;
				command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						_runningThread=false;
						String text;

						try {
							text = command.get().get(0);

							//checks if search was successful or not
							if(text.contentEquals(_term + " not found :^(" )) {
								error("search term not found");
								searchButton.setText("search");

							}else {
								chooseLines(text);
							}
						} catch (InterruptedException | ExecutionException e) {
							error("search was interrupted");
						}
					}
				});
			}
		});


		HBox actionButtons = new HBox(returnButton,searchButton);
		_pane.setBottom(actionButtons);

		_primaryStage.setScene(new Scene(_pane,400,400));
		_primaryStage.show();
	}


	/**
	 * User is asked to choose the number of lines they want to include in their creation
	 * @param text
	 */
	public void chooseLines(String text) {


		Text titleText = new Text("How Many Lines Do you Want in your Creation?");
		titleText.setFont(Font.font ("Verdana", 20));
		titleText.wrappingWidthProperty().bind(_pane.widthProperty());
		TextField fieldInput = new TextField();
		VBox topBox = new VBox(titleText,fieldInput);
		_pane.setTop(topBox);


		String[] lines = text.split("\\.");
		text="";

		for(int i = 0; i<lines.length;i++) {
			text=text+ (i+1)+") " + lines[i]+"\n";
		}

		//text is presetned in a scrollpane to allow for overflowing text.
		ScrollPane scrollPane = new ScrollPane();
		Text creationContent = new Text(text);
		creationContent.wrappingWidthProperty().bind(_pane.widthProperty());
		scrollPane.setContent(creationContent);
		_pane.setCenter(scrollPane);

		Button returnButton = new Button("return to menu");
		returnButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(_runningThread) {
					error("please wait for process the finish");
					return;
				}
				MainMenu menu = new MainMenu(_primaryStage);
				menu.displayMenu();
			}
		});

		Button continueButton = new Button("Continue");
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(_runningThread) {
					error("please wait for process the finish");
					return;
				}
				
				String term = fieldInput.getCharacters().toString();
				
				//determines if the input was a number
				if(term.isEmpty() || !term.chars().allMatch(Character::isDigit)) {
					error("please enter a number");
					return;

					//determines if the number input is in the valid range of lines
				}else if(Integer.parseInt(term)<=lines.length  && Integer.parseInt(term)>0) {
					String selectedText="";
					for(int i =0;i<Integer.parseInt(term);i++) {
						selectedText=selectedText+lines[i];
					}

					//audio file is created from the specified number of lines 
					RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./temp/temptts.wav");
					_team.submit(audioCreation);
					_runningThread = true;
					audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							_runningThread=false;
							nameVideo(lines,Integer.parseInt(term));
						}
					});

				}else {
					error("please enter a valid number");
				}
			}
		});

		HBox actionButtons = new HBox(returnButton,continueButton);
		_pane.setBottom(actionButtons);
	}

	/**
	 * User is asked to name their videoCreation. 
	 * If the name already exists, the user will be asked if they want to overwrite or rename
	 * @param _pane
	 * @param text
	 * @param count
	 */
	public void nameVideo(String[] text,int count) {

		Text titleText = new Text("What would you like to name your Creation?");
		titleText.setFont(Font.font ("Verdana", 20));
		titleText.wrappingWidthProperty().bind(_pane.widthProperty());
		TextField nameField = new TextField();

		VBox topBox = new VBox(titleText,nameField);
		_pane.setTop(topBox);

		VBox center = new VBox();
		_pane.setCenter(center);

		Button continueButton = new Button("continue");
		Button returnButton = new Button("return to menu");
		returnButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(_runningThread) {
					error("please wait for process to finish");
					return;
				}
				MainMenu menu = new MainMenu(_primaryStage);
				menu.displayMenu();
			}
		});

		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {		
				if(_runningThread) {
					error("please wait for process to finish");
					return;
				}
				String name = nameField.getCharacters().toString();

				if(name.isEmpty()) {
					error("Name cannot be empty");
				}else if((!name.matches("[a-zA-Z0-9_-]*"))) {
					error("name can only contain letter, numbers, _ and - ");
				}else{

					//Check if the file already exists
					File f = new File("./VideoCreations/"+name+".mp4");
					if(f.exists()) {

						//prompts user if they want to overwrite or rename
						Text error = new Text("ERROR: FileName Aready Exists, Would you like to Overwrite or Rename?");
						error.wrappingWidthProperty().bind(_pane.widthProperty());
						Button owButton = new Button("Overwrite");
						owButton.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
					
								Text confirm = new Text("Are you sure you want to OVERWRITE "+name+"? ");

								Button yesButton = new Button("yes");
								Button noButton = new Button("no");

								yesButton.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent event) {
										if(_runningThread) {
											error("Please wait for process to finish");
											return;
										}//remove existing video file, so new one can be made smoothly.
										_team.submit(new RunBash("rm -f ./VideoCreations/"+name+".mp4"));
										continueButton.setText("Creating...");
										createVideo(name);
									}
									
								});
								
								noButton.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent event) {
										if(_runningThread) {
											error("Please wait for process to finish");
											return;
										}
										nameVideo(text,count);
									}
								});
								
								_pane.setCenter(new HBox(confirm, yesButton, noButton));
							}
						});

						Button renameButton = new Button("Rename");
						renameButton.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								if(_runningThread) {
									error("Please wait for process to finish");
									return;
								}
								nameVideo(text,count);
							}
						});

						HBox options = new HBox(owButton,renameButton);
						VBox centreBox = new VBox(error,options);
						_pane.setCenter(centreBox);
					}else {
						continueButton.setText("Creating...");
						createVideo(name);
					}
				}
			}
		});


		HBox actionButtons = new HBox(returnButton,continueButton);
		_pane.setBottom(actionButtons);
	}

	/**
	 * creates a video with all given inputs, using the bash command ffmpeg.
	 * @param name
	 */
	public void createVideo(String name) {
		RunBash audioLengthSoxi = new RunBash("soxi -D ./temp/temptts.wav");

		_team.submit(audioLengthSoxi);
		_runningThread = true;
		audioLengthSoxi.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_runningThread=false;
				double audioLength;

				try {
					audioLength = Double.parseDouble(audioLengthSoxi.get().get(0));
					RunBash createVideo = new RunBash("ffmpeg -i ./temp/temptts.wav -vn -ar 44100 -ac 2 -b:a 192k ./temp/temptts.mp3 &> /dev/null "
							+ "; ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
							+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
							+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_term+"\" ./temp/"+name+".mp4 &> /dev/null "
							+ "; ffmpeg -i ./temp/"+name +".mp4 -i ./temp/temptts.mp3 -c:v copy -c:a aac -strict experimental "
							+ "./VideoCreations/"+name+".mp4  &> /dev/null");
					_team.submit(createVideo);
					_runningThread = true;
					createVideo.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							_runningThread=false;
							success(_pane, name);
						}
					});
				} catch (NumberFormatException | InterruptedException | ExecutionException e) {
					error("Video Creation Failed");
				}
			}
		});
	}

	/**
	 * Creates the final scene where a success message is displayed to tell the user their creation has been created.
	 * @param _pane
	 * @param name
	 */
	public void success(BorderPane _pane, String name) {

		Text titleText = new Text("Successfully created creation called " + name);
		titleText.setFont(Font.font ("Verdana", 20));
		titleText.wrappingWidthProperty().bind(_pane.widthProperty());
		VBox topBox = new VBox(titleText);
		_pane.setTop(topBox);

		Button returnButton = new Button("return to menu");
		returnButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				MainMenu menu = new MainMenu(_primaryStage);
				menu.displayMenu();

			}
		});
		_pane.setCenter(new HBox());
		HBox bottom = new HBox(returnButton);
		_pane.setBottom(bottom);		
	}

	/**
	 * creates an error popup with the given string input
	 * @param msg
	 */
	public void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

}
