package Code.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import Code.Entity.Entity;
import Code.Entity.Moveable.Player;
import Code.Entity.Moveable.Enemies.*;
import Code.Entity.Non_moveable.Brick;
import Code.Entity.Non_moveable.Non_moveable;
import Code.Entity.Non_moveable.Wall;
import Code.Entity.Non_moveable.Items.bonusHealth;
import Code.Entity.Non_moveable.Items.increaseRange;
import Code.Entity.Non_moveable.Items.speedUp;
import Code.Entity.ShortLife.Bom;
import Code.Entity.ShortLife.Fire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.text.Font;

public class Game {
    private BackgroundImage background = new BackgroundImage(new Image("./Resources/icons/background.jpg"), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
    public static final int WIDTH = 20, HEIGHT = 12, CELLS_SIZE = 30, FPS = 30, TPS = 30;
    private String mapPath;
    private Scene scene;
    private Clip clip;
    private AudioInputStream music;
    private List<Integer> listScore = new ArrayList<>();
    private List<KeyCode> input = new ArrayList<>();
    private Entity[][] map;
    private Player player;
    private List<Enemy> enemies;

    private class MyThread extends Thread {
        private Object o = new Object();
        private boolean lock = false;

        public void check() {
            if (lock) synchronized (o) {
                try {
                    o.wait();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        public void pause() {
            lock = true;
        }

        public void customeResume() {
            lock = false;
            synchronized (o) {
                o.notify();
            }
        }

    }

    /** main loop */
    private MyThread drawThread, tickThread;
    final String TEXT_FILL = "-fx-text-fill: #fff;";
    
    /** play */
    private Image grass, hearthIcon;
    private AnchorPane playPane, playPausePane;
    private Canvas canvas;
    private GraphicsContext gc;
    private Button resumeBtn, fromPlayToMenuBtn, restartBtn;
    private ImageView health1, health2, health3, pauseBtn;

    /** setting */
    private AnchorPane settingPane;
    private Label settingTitle;
    private ImageView level1, level2;

    /** score */
    private AnchorPane scorePane;
    private Label scoreTitle;

    /** help */
    private AnchorPane helpPane;
    private Button helpUpBtn, helpDownBtn, helpLeftBtn, helpRightBtn, helpSpaceBtn, BTMenu;
    private Label helpUp, helpDown, helpLeft, helpRight, helpSpace, helpBottomTitle, tutorial;

    /** menu */
    private AnchorPane menuPane;
    private Label menuNameGameLabel;
    private Button menuHelpBtn, menuScoreBtn, menuSettingBtn, menuPlayBtn;
    
    /** tạo Button dễ hơn */
    private Button newButton(String text, double preX, double preY, double layoutX, double layoutY) {
        Button ans = new Button(text);
        ans.setPrefSize(preX, preY);
        ans.setLayoutX(layoutX);
        ans.setLayoutY(layoutY);
        return ans;
    }

    /** tạo Label dễ hơn */
    private Label newLabel(String text, double preX, double preY, double layoutX, double layoutY) {
        Label ans = new Label(text);
        ans.setPrefSize(preX, preY);
        ans.setAlignment(Pos.CENTER);
        ans.setLayoutX(layoutX);
        ans.setLayoutY(layoutY);
        return ans;
    }

    /** tạo ảnh dễ hơn */
    private ImageView newImageView(Image img, double preX, double preY, double layoutX, double layoutY) {
        ImageView ans = new ImageView(img);
        ans.setFitHeight(preY);
        ans.setFitWidth(preX);
        ans.setLayoutX(layoutX);
        ans.setLayoutY(layoutY);
        return ans;
    }

    private void setupMenuPane() {
        menuNameGameLabel = newLabel("Bomberman", 200, 100, 40, 24);
        menuNameGameLabel.getStyleClass().add("name");
        menuHelpBtn = newButton("Help", 120, 30, 70, 320 - 70);
        menuHelpBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    setupHelpPane();
                    scene.setRoot(helpPane);
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });

        menuScoreBtn = newButton("Score", 120, 30, 70, 280 - 70);
        menuScoreBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    setupScorePane();
                    scene.setRoot(scorePane);
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });

        menuSettingBtn = newButton("Options", 120, 30, 70, 240 - 70);
        menuSettingBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    setupSettingPane();
                    scene.setRoot(settingPane);
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });

        menuPlayBtn = newButton("Play", 120, 30, 70, 200 - 70);
        menuPlayBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    mapPath = System.getProperty("user.dir") +  "/src/Resources/data/map1.txt";
                    setupPlayPane();
                    scene.setRoot(playPane);
                    start();
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });
        
        menuPane = new AnchorPane();
        menuPane.setPrefSize(640, 400);
        menuPane.setBackground(new Background(background));
        menuPane.getChildren().addAll(menuHelpBtn, menuScoreBtn, menuSettingBtn, menuPlayBtn, menuNameGameLabel);
    }

    private void setupPlayPane() {
        grass = new Image("./Resources/icons/grass.png");
        hearthIcon = new Image("./Resources/icons/heart.png");
        health1 = newImageView(hearthIcon, 20, 20, 20, 0);
        health2 = newImageView(hearthIcon, 20, 20, 40, 0);
        health3 = newImageView(hearthIcon, 20, 20, 60, 0);

        canvas = new Canvas(CELLS_SIZE*WIDTH, CELLS_SIZE*HEIGHT);
        canvas.setLayoutX(20);
        canvas.setLayoutY(20);
        gc = canvas.getGraphicsContext2D();

        pauseBtn = newImageView(new Image("./Resources/icons/setting-icon.png"), 20, 20, 620, 0);
        pauseBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                playPausePane.setVisible(true);
                pause();
            }
        });

        restartBtn = newButton("Restart", 100, 20, 0, 40);
        restartBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                playPausePane.setVisible(false);
                start();
            }
        });

        resumeBtn = newButton("Resume", 100, 20, 0, 0);
        resumeBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                playPausePane.setVisible(false);
                resume();
            }
        });
    
        fromPlayToMenuBtn = newButton("Back to menu", 100, 20, 0, 80);
        fromPlayToMenuBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scene.setRoot(menuPane);
            }
        });

        playPausePane = new AnchorPane();
        playPausePane.setPrefSize(100, 100);
        playPausePane.setLayoutX(270);
        playPausePane.setLayoutY(150);
        playPausePane.setVisible(false);
        playPausePane.getChildren().addAll(resumeBtn, fromPlayToMenuBtn, restartBtn);

        playPane = new AnchorPane();
        playPane.setBackground(new Background(background));
        playPane.setPrefSize(640, 400);
        playPane.getChildren().addAll(canvas, playPausePane, pauseBtn, health1, health2, health3);
    }

    private void setupSettingPane() {
        settingTitle = newLabel("CHOOSE MAP", 240, 100, 200, 0);
        settingTitle.getStyleClass().add("map");
        settingTitle.setFont(new Font("Franklin Gothic Medium", 100));

        level1 = newImageView(new Image("./Resources/icons/Map1.png") ,200, 147, 62, 127);
        level1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    mapPath = System.getProperty("user.dir") +  "/src/Resources/data/map1.txt";
                    setupPlayPane();
                    scene.setRoot(playPane);
                    start();
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });

        level2 = newImageView(new Image("./Resources/icons/Map2.png") ,200, 147, 371, 127);
        level2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    mapPath = System.getProperty("user.dir") +  "/src/Resources/data/map2.txt";
                    setupPlayPane();
                    scene.setRoot(playPane);
                    start();
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }
        });

        BTMenu = newButton("Back to menu", 120, 31, 476, 361);
        BTMenu.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scene.setRoot(menuPane);
            }
        });

        settingPane = new AnchorPane();
        settingPane.setBackground(new Background(background));
        settingPane.setPrefSize(640, 400);
        settingPane.getChildren().addAll(settingTitle, level1, level2, BTMenu);
    }

    private int getScore() {
        int ans = 100 + player.getHealth()*20 - enemies.size()*10;
        return ans;
    }

    private void setupScorePane() {
        loadListScore();

        scoreTitle = newLabel("Score",166, 50, 213, 29);
        scoreTitle.getStyleClass().add("point");
        scoreTitle.setFont(new Font("Franklin Gothic Medium", 42));

        ListView<Object> lsScore = new ListView<>();
        lsScore.setPrefSize(200, 281);
        lsScore.setLayoutX(70);
        lsScore.setLayoutY(100);
        lsScore.getSelectionModel().selectIndices(0, 9);
        ObservableList<Object> b = FXCollections.observableArrayList();
        for (int i: listScore)
            b.add(i);
        lsScore.setItems(b);

        ListView<Object> highestScore = new ListView<>();
        highestScore.setPrefSize(200, 74);
        highestScore.setLayoutX(280);
        highestScore.setLayoutY(100);
        ImageView gold = newImageView(new Image("Resources/icons/goldMedal.png"), 35, 25, 455, 100);
        ImageView silver = newImageView(new Image("Resources/icons/silverMedal.png"), 35, 23, 455, 125);
        ImageView bronze = newImageView(new Image("Resources/icons/bronzeMedal.png"), 35, 20, 455, 150);
        
        ObservableList<Object> c = FXCollections.observableArrayList();
        ArrayList<Integer> copy = new ArrayList<>();
        copy.addAll(listScore);
        for (int i = 0; i < 3 && i < copy.size(); i++) {
            int max = Collections.max(copy);
            c.add(max);
            copy.remove(max);
        }
        highestScore.setItems(c);

        BTMenu = newButton("Back to menu", 120, 31, 476, 361);
        BTMenu.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scene.setRoot(menuPane);
            }
        });

        scorePane = new AnchorPane();
        scorePane.setBackground(new Background(background));
        scorePane.setPrefSize(640, 400);
        scorePane.getChildren().addAll(scoreTitle, lsScore, highestScore, BTMenu, gold, silver, bronze);
    }

    private void setupHelpPane() {
        tutorial = newLabel("Tutorial",166, 50, 213, 29);
        tutorial.getStyleClass().add("help");
        tutorial.setFont(new Font("Franklin Gothic Medium", 42));

        helpUpBtn = newButton("^", 44, 31, 150, 118);
        helpDownBtn = newButton("v", 44, 31, 150, 149);
        helpLeftBtn = newButton("v", 44, 31, 106, 149);
        helpRightBtn = newButton("v", 44, 31, 194, 149);
        helpSpaceBtn = newButton("", 187, 31, 74, 215);

        helpUp = newLabel("Press button '^' to go up ", 174, 21, 351, 95);
        helpUp.getStyleClass().add("text-fill1");

        helpDown = newLabel("Press button 'v' to go down ", 194, 21, 351, 123);
        helpDown.getStyleClass().add("text-fill2");

        helpLeft = newLabel("Press button '<' to go left ", 180, 21, 351, 180);
        helpLeft.getStyleClass().add("text-fill3");

        helpRight = newLabel("Press button '>' to go right ", 187, 21, 351, 154);
        helpRight.getStyleClass().add("text-fill4");

        helpSpace = newLabel("Press button SPACE to drop your bomb", 278, 21, 300, 220);
        helpSpace.getStyleClass().add("text-fill5");

        helpBottomTitle = newLabel("You must kill all the enemies to win this game!", 426, 27, 107, 308);
        helpBottomTitle.getStyleClass().add("text-fill6");
        helpBottomTitle.setFont(new Font("Book Antiqua", 20));

        BTMenu = newButton("Back to menu", 120, 31, 476, 361);
        BTMenu.getStyleClass().add("back-to-menu-button");
        BTMenu.setOnMouseClicked(event -> scene.setRoot(menuPane));

        helpPane = new AnchorPane();
        helpPane.setBackground(new Background(background));
        helpPane.setPrefSize(640, 400);
        helpPane.getChildren().addAll(tutorial, helpUpBtn, helpDownBtn, helpLeftBtn, helpRightBtn, helpSpaceBtn, helpUp, helpDown, helpLeft, helpRight, helpSpace, helpBottomTitle, BTMenu);
    }

    private void setupGame() {
        try {
            resumeBtn.setDisable(false);

            // khởi tạo luồng loop
            tickThread = new MyThread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            check();

                            long start = System.nanoTime();
    
                            if (!player.isAlive() || enemies.isEmpty())
                                end();
    
                            // enemies
                            checkTouchEnemy();
                            for (int i = 0; i < enemies.size(); i++)
                                if (enemies.get(i).isAlive())
                                    enemies.get(i).move(map);
                                else {
                                    enemies.remove(i);
                                    i--;
                                }
    
                            // moveable
                            if (input.contains(KeyCode.LEFT))
                                player.moveLeft(map);
                            if (input.contains(KeyCode.RIGHT))
                                player.moveRight(map);
                            if (input.contains(KeyCode.UP))
                                player.moveUp(map);
                            if (input.contains(KeyCode.DOWN))
                                player.moveDown(map);
    
                            // map
                            for (int i = 0; i < HEIGHT; i++)
                                for (int j = 0; j < WIDTH; j++)
                                    if (map[i][j] != null)
                                        if (map[i][j].isAlive())
                                            map[i][j].update(player);
                                        else if (map[i][j] instanceof Bom)
                                            ignite(i, j);
                                        else map[i][j] = null;
    
                            long elapsed = System.nanoTime() - start;
                            if (elapsed < 1000000000/TPS)
                                Thread.sleep((1000000000/TPS - elapsed)/1000000);
                        } catch (Exception e) {
                            System.out.print(e.getMessage());
                        }
                    }
                }
            };
            tickThread.setDaemon(true);

            drawThread = new MyThread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            check();

                            long start = System.nanoTime();

                            // back grass
                            for (int i = 0; i < HEIGHT; i++)
                                for (int j = 0; j < WIDTH; j++)
                                    gc.drawImage(grass, j*CELLS_SIZE, i*CELLS_SIZE);

                            // map
                            for (int i = 0; i < HEIGHT; i++)
                                for (int j = 0; j < WIDTH; j++)
                                    if (map[i][j] != null && map[i][j].isAlive())
                                        map[i][j].render(gc);

                            // health symbol
                            health1.setVisible(player.getHealth() >= 1);
                            health2.setVisible(player.getHealth() >= 2);
                            health3.setVisible(player.getHealth() >= 3);

                            player.render(gc);
                            enemies.forEach(i -> i.render(gc));

                            long elapsed = System.nanoTime() - start;
                            if (elapsed < 1000000000/FPS)
                                Thread.sleep((1000000000/FPS - elapsed)/1000000);
                        } catch (Exception e) {
                            System.out.print(e.getMessage());
                        }
                    }
                }
            };
            drawThread.setDaemon(true);

            // khởi tạo player
            player = new Player(CELLS_SIZE, CELLS_SIZE);

            // khởi tạo map
            loadMap(mapPath);

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    /** khởi tạo game */
    public Game() {
        try {
            // khởi tạo menu
            setupMenuPane();

            // load score
            loadListScore();
            
            // setup main scene
            scene = new Scene(menuPane);
            scene.getStylesheets().add(getClass().getResource("GUI.css").toExternalForm());
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE)
                    map[(player.getY()+CELLS_SIZE/2)/CELLS_SIZE][(player.getX()+CELLS_SIZE/2)/CELLS_SIZE]
                        = new Bom(((player.getX()+CELLS_SIZE/2) / CELLS_SIZE) * CELLS_SIZE,
                            ((player.getY()+CELLS_SIZE/2) / CELLS_SIZE) * CELLS_SIZE);
                else if (!input.contains(event.getCode()))
                    input.add(event.getCode());
            });
            scene.setOnKeyReleased(event -> {
                if (event.getCode() != KeyCode.SPACE)
                    input.remove(event.getCode());
            });
        
            // sound
            playSound("Jumper");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void playSound(String fileName) {
        try {
            if (clip != null)
                clip.stop();
            clip = AudioSystem.getClip();
            music = AudioSystem.getAudioInputStream(new File("./src/Resources/sound/" + fileName + ".wav"));
            clip.open(music);
            clip.loop(100);
            clip.start();
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void loadListScore() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(System.getProperty("user.dir") +  "/src/Resources/data/Score.txt")));
            listScore = (List<Integer>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    public Scene getScene() {
        return scene;
    }

    private void kill(Fire fire) throws Exception {
        fire.burn(player);
        enemies.forEach(i -> {
            try {
                fire.burn(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void ignite(int i, int j) throws Exception {
        // play sound
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(System.getProperty("user.dir") + "/src/Resources/sound/explosion.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        map[i][j] = new Fire(j*CELLS_SIZE, i*CELLS_SIZE);
        kill((Fire) map[i][j]);

        // left
        for (int x = 1; x <= player.getIgniteRange(); x++)
            if (map[i][j-x] == null) {
                map[i][j-x] = new Fire((j-x)*CELLS_SIZE, i*CELLS_SIZE);
                kill((Fire) map[i][j-x]);
            } else if ((map[i][j-x] instanceof Non_moveable) && map[i][j-x].canBeBurn()) {
                map[i][j-x] = brickDestroyed(i, j-x);
                break;
            } else break;

        // right
        for (int x = 1; x <= player.getIgniteRange(); x++)
            if (map[i][j+x] == null) {
                map[i][j+x] = new Fire((j+x)*CELLS_SIZE, i*CELLS_SIZE);
                kill((Fire) map[i][j+x]);
            } else if ((map[i][j+x] instanceof Non_moveable) && map[i][j+x].canBeBurn()) {
                map[i][j+x] = brickDestroyed(i, j+x);
                break;
            } else break;
        
        // up
        for (int x = 1; x <= player.getIgniteRange(); x++)
            if (map[i-x][j] == null) {
                map[i-x][j] = new Fire(j*CELLS_SIZE, (i-x)*CELLS_SIZE);
                kill((Fire) map[i-x][j]);
            } else if ((map[i-x][j] instanceof Non_moveable) && map[i-x][j].canBeBurn()) {
                map[i-x][j] = brickDestroyed(i-x, j);
                break;
            } else break;
        
        // down
        for (int x = 1; x <= player.getIgniteRange(); x++)
            if (map[i+x][j] == null) {
                map[i+x][j] = new Fire(j*CELLS_SIZE, (i+x)*CELLS_SIZE);
                kill((Fire) map[i+x][j]);
            } else if ((map[i+x][j] instanceof Non_moveable) && map[i+x][j].canBeBurn()) {
                map[i+x][j] = brickDestroyed(i+x, j);
                break;
            } else break;
    }

    private Entity brickDestroyed(int i, int j) {
        if (StdRandom.uniform(250) > 24) {
            return new Fire(j*CELLS_SIZE, i*CELLS_SIZE);
        } else {
            int rand = StdRandom.uniform(3);
            switch (rand) {
                case 1:
                    return new bonusHealth(j*CELLS_SIZE, i*CELLS_SIZE);
                case 2:
                    return new increaseRange(j*CELLS_SIZE, i*CELLS_SIZE);
                default:
                    return new speedUp(j*CELLS_SIZE, i*CELLS_SIZE);
            }
        }
    }

    private void start() {
        playPausePane.setVisible(false);
        setupGame();
        tickThread.start();
        drawThread.start();
    }

    private void pause() {
        tickThread.pause();
        drawThread.pause();
    }

    private void resume() {
        tickThread.customeResume();
        drawThread.customeResume();
    }

    private void end() {
        try {
            listScore.add(getScore());
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(System.getProperty("user.dir") +  "/src/Resources/data/Score.txt")));
            out.writeObject(listScore);
            out.close();
            playPausePane.setVisible(true);
            resumeBtn.setDisable(true);

            tickThread.pause();
            drawThread.pause();
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void checkTouchEnemy() {
        for (Enemy i: enemies) {
            double x = Math.abs(player.getX() - i.getX());
            double y = Math.abs(player.getY() - i.getY());
            if (x < (3*CELLS_SIZE)/5 && y < (3*CELLS_SIZE)/5) {
                try {
                    player.die();
                    i.die();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadMap(String path) {
        try {
            map = new Entity[HEIGHT][WIDTH];
            enemies = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader(path));
            String bufferString;
            for (int i = 0; i < HEIGHT; i++) {
                bufferString = br.readLine();
                for (int j = 0; j < WIDTH; j++) {
                    if (bufferString.charAt(j) == 'w') map[i][j] = new Wall(j * CELLS_SIZE, i * CELLS_SIZE);
                    else if (bufferString.charAt(j) == 'b') map[i][j] = new Brick(j * CELLS_SIZE, i * CELLS_SIZE);
                }
            }

            while (br.ready()) {
                String[] a = br.readLine().split(" ");
                int x = Integer.parseInt(a[0]) * CELLS_SIZE;
                int y = Integer.parseInt(a[1]) * CELLS_SIZE;
                createEnemy(x, y);
            }

            br.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    private void createEnemy(int x, int y) {
        switch (StdRandom.uniform(5)) {
            case 1:
                enemies.add(new Balloom(x, y, StdRandom.uniform(3) + 1));
                break;
            case 2:
                enemies.add(new Doll(x, y, StdRandom.uniform(3) + 1));
                break;
            case 3:
                enemies.add(new Kondoria(x, y, StdRandom.uniform(3) + 1));
                break;
            case 4:
                enemies.add(new Minvo(x, y, StdRandom.uniform(3) + 1));
                break;
            default:
                enemies.add(new Oneal(x, y, StdRandom.uniform(3) + 1));
                break;
        }
    }
}