package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.AndroidGame;
import com.mygdx.game.UI.ClickCallback;
import com.mygdx.game.UI.ControlModeSelectButton;
import com.mygdx.game.UI.SimpleLabel;
import com.mygdx.game.assets.Assets;
import com.mygdx.game.controllers.PlatformController;
import com.mygdx.game.controllers.RandomObjectsController;
import com.mygdx.game.entities.Background;
import com.mygdx.game.entities.BonusObject;
import com.mygdx.game.entities.Ground;
import com.mygdx.game.entities.JumpPlayer;
import com.mygdx.game.entities.Platform;
import com.mygdx.game.entities.Walls;

import java.util.ArrayList;

import static com.mygdx.game.AndroidGame.GRAVITY;
import static com.mygdx.game.AndroidGame.SCREEN_X;
import static com.mygdx.game.AndroidGame.SCREEN_Y;

/**
 * Created by ksikorski on 09.12.2016.
 */

public class GameScreen extends AbstractScreen {

    private PlatformController platformController;

    public Texture grassTexture;
    private Texture backgroundTexture;
    private Texture groundTexture;
    private Texture logTexture;
    private Texture backButtonTexture;
    private Texture arrowLeftTexture;
    private Texture arrowRightTexture;

    private SimpleLabel accXValueLabel, scoreLabel, bestScoreLabel;
    private ControlModeSelectButton controlModeSelectButton;
    private Background background;
    private Ground ground;
    private Walls walls;
    private ImageButton backButton;
    private Image arrowLeft;
    private Image arrowRight;

    private RandomObjectsController randomObjectsController;

    private Float averageAccX = 0.0f;


    public GameScreen(AndroidGame game, JumpPlayer player) {
        super(game, player);
        game.scoreService.resetScore();

        assignDataToVariables();
        initBackground();
        initGround();
        initWalls();

        initBackToMenuButton();
        initPlatformService();
        initControlTypeSelectButton();
        initAccXValueLabel();
        initArrows();

        initScoreLabel();
        initBestScoreLabel();

        initRandomObjectsController();
        stage.addActor(player);

    }

    private void initArrows() {
        arrowLeft = new Image(arrowLeftTexture);
        arrowRight = new Image(arrowRightTexture);
        if (game.controlMode == AndroidGame.ControlMode.MANUAL){

            arrowLeft.setHeight(180);
            arrowLeft.setWidth(100);

            arrowRight.setHeight(180);
            arrowRight.setWidth(100);

            stage.addActor(arrowLeft);
            stage.addActor(arrowRight);
        }
    }

    private void initBackToMenuButton() {
        Drawable drawable = new TextureRegionDrawable(new TextureRegion(backButtonTexture));
        backButton = new ImageButton(drawable);
        backButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.scoreService.saveScore();
                game.setScreen(new MainMenuScreen(game, new JumpPlayer(game.playerTexture)));
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        stage.addActor(backButton);
    }

    private void initRandomObjectsController() {
        randomObjectsController = new RandomObjectsController(game, stage, player);
    }

    private void initPlatformService() {
        platformController = new PlatformController(this, stage);
    }

    private void initWalls() {
        walls = new Walls(logTexture, stage);
        stage.addActor(walls.leftWall);
        stage.addActor(walls.rightWall);
    }

    private void initGround() {
        ground = new Ground(groundTexture);
        stage.addActor(ground);
    }

    private void initBackground() {
        background = new Background(backgroundTexture);
        stage.addActor(background);
    }

    private void initControlTypeSelectButton() {
        controlModeSelectButton = new ControlModeSelectButton(new ClickCallback() {
            @Override
            public void onClick() {
                switch (game.controlMode) {
                    case ACCELEROMETER: {
                        game.controlMode = AndroidGame.ControlMode.MANUAL;
                        break;
                    }
                    case MANUAL: {
                        game.controlMode = AndroidGame.ControlMode.ACCELEROMETER;
                        break;
                    }
                }
            }
        });
    }

    private void assignDataToVariables() {
        grassTexture = Assets.sharedInstance.assetManager.get("textures/grass.png",Texture.class);
        backgroundTexture = Assets.sharedInstance.assetManager.get("textures/clouds.png",Texture.class);
        groundTexture = Assets.sharedInstance.assetManager.get("textures/grass2.png",Texture.class);
        logTexture = Assets.sharedInstance.assetManager.get("textures/log.png",Texture.class);
        backButtonTexture = Assets.sharedInstance.assetManager.get("textures/backToMenu.png",Texture.class);
        arrowLeftTexture = Assets.sharedInstance.assetManager.get("textures/arrow-left.png",Texture.class);
        arrowRightTexture = Assets.sharedInstance.assetManager.get("textures/arrow-right.png",Texture.class);
    }

    private void initAccXValueLabel() {
//        accXValueLabel = new SimpleLabel("");
//        stage.addActor(accXValueLabel);
    }

    private void initScoreLabel() {
        scoreLabel = new SimpleLabel("");
        stage.addActor(scoreLabel);
    }

    private void initBestScoreLabel() {
        bestScoreLabel = new SimpleLabel("");
        stage.addActor(bestScoreLabel);
    }

    /* Main render method */
    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        checkDependencies();

        batch.begin();
        stage.draw();
        batch.end();
    }

    /* objects (images, labels, buttons etc.) position update methods */
    private void update() {
        handleInput();
        updateScoreLabel();

        backgroundPositionUpdate();
        wallsPositionUpdate();
        labelsPositionUpdate();
        playerJumpPositionUpdate();
        buttonsPositionUpdate();

        arrowsUpdate();
        backButton.setY(camera.position.y + 330);
        backButton.setX(camera.position.x + 20);
        stage.act();
    }

    private void arrowsUpdate() {
        arrowLeft.setY(camera.position.y - 300);
        arrowLeft.setX(camera.position.x - 250);

        arrowRight.setY(camera.position.y - 300);
        arrowRight.setX(camera.position.x + 150);
    }

    private void checkDependencies(){
        checkPlatformAndPlayerDependencies();
        checkIfPlayerFellTooLow();
        checkBonusObjectsAndPlayerDependencies();
    }

    private void wallsPositionUpdate() {
        walls.leftWall.setY(camera.position.y - SCREEN_Y);
        walls.rightWall.setY(camera.position.y - SCREEN_Y);
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.scoreService.getPoints());
        bestScoreLabel.setText("Your best: " + game.scoreService.getBestScore());
    }

    private void backgroundPositionUpdate() {
        background.setX(camera.position.x - SCREEN_X / 2);
        background.setY(camera.position.y - SCREEN_Y / 2);
    }

    private void labelsPositionUpdate() {
//        accXValueLabel.setPosition(camera.position.x + 100, camera.position.y + 400);
        scoreLabel.setPosition(camera.position.x - 280, camera.position.y + 460);
        bestScoreLabel.setPosition(camera.position.x - 280, camera.position.y + 425);
    }

    private void buttonsPositionUpdate() {
		controlModeSelectButton.setPosition(camera.position.x + 100, camera.position.y + 250);
    }

    /* Handle player jumping animation */
    private void playerJumpPositionUpdate() {
        player.setY(player.getY() + (player.jumpSpeed * Gdx.graphics.getDeltaTime()));

        if(player.getY() > 0){
            player.jumpSpeed += GRAVITY;
        } else {
            player.setY(0);
            player.canJump = true;
            player.jumpSpeed = 0;
        }

        player.setGreatestHeight(player.getY());
    }

    private void checkPlatformAndPlayerDependencies() {
        platformController.generateMorePlatforms();

        ArrayList<Platform> platformsToRemove = new ArrayList<Platform>();
        for (Platform platform : platformController.platformArray){
            if(player.getY() - platform.getY() > 600) {
                platform.addAction(Actions.removeActor());
                platformController.platformArray.remove(platform);
                break;
            }

            if(isPlayerOnPlatform(platform)){
                addPlatformPoints(platform);
                player.setY(platform.getY() + platform.getHeight()-10);
                player.canJump = true;
                player.jumpSpeed = 0;
                if (platform.type == Platform.PlatformType.DISINTEGRATING) {
                    platform.disintegrate();
                    platformsToRemove.add(platform);
                }
            }
        }

        platformController.platformArray.removeAll(platformsToRemove);
    }

    private void checkBonusObjectsAndPlayerDependencies() {
        ArrayList<BonusObject> objectsToRemove = new ArrayList<BonusObject>();
        for (BonusObject bonusObject : randomObjectsController.bonusList){
            if(isPlayerOverlappingBonusObject(bonusObject)){
                checkIfPaprika(bonusObject);
                addBonusPoints(bonusObject);
                bonusObject.playSound();
                bonusObject.addAction(Actions.removeActor());
                objectsToRemove.add(bonusObject);
            }
        }
        randomObjectsController.bonusList.removeAll(objectsToRemove);
    }

    private void checkIfPaprika(BonusObject bonusObject) {
        if (bonusObject.type == BonusObject.BonusType.POISON){
            game.scoreService.saveScore();
            game.setScreen(new EndGameScreen(game, new JumpPlayer(game.playerTexture)));
        }
    }

    private void checkIfPlayerFellTooLow() {
        if (player.getGreatestHeight() - player.getY() > 800){
            game.scoreService.saveScore();
            game.setScreen(new EndGameScreen(game, new JumpPlayer(game.playerTexture)));
        }
    }

    private void addBonusPoints(BonusObject bonusObject) {
        game.scoreService.addPoints(20);
    }

    private void addPlatformPoints(Platform p) {
        if (!p.reached) {
            game.scoreService.addPlatformPoints();
            p.reached = true;
        }
    }

    /* Stops falling down if on a platform */
    private boolean isPlayerOnPlatform(Platform platform) {
        int collisionFix = 30;
        Rectangle rectPlayer = new Rectangle(player.getX() + collisionFix, player.getY() + collisionFix, player.getWidth() - collisionFix, player.getHeight() - collisionFix);
        Rectangle rectPlatform = new Rectangle(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        return player.jumpSpeed <= 0 && rectPlayer.overlaps(rectPlatform) && rectPlayer.getY() > rectPlatform.getY() + rectPlatform.getHeight() - 20;
    }

    /* Stops falling down if on a platform */
    private boolean isPlayerOverlappingBonusObject(BonusObject bonusObject) {
        int collisionFix = 20;
        Rectangle rectPlayer = new Rectangle(player.getX() + collisionFix, player.getY() + collisionFix, player.getWidth() - collisionFix, player.getHeight() - collisionFix);
        Rectangle rectBonusObject = new Rectangle(bonusObject.getX(), bonusObject.getY(), bonusObject.getWidth(), bonusObject.getHeight());
        return rectBonusObject.overlaps(rectPlayer);
    }

    /* Input handling methods */
    private void handleInput() {

        /* Accelerometer handler */
        if (game.controlMode == AndroidGame.ControlMode.ACCELEROMETER) {
            float accelerometerX = Gdx.input.getAccelerometerX();
//            accXValueLabel.setText(Float.toString(accelerometerX));

            /* Weighted arithmetic mean for smooth player animation */
            averageAccX = (accelerometerX + (3 * averageAccX) / 4);

            if(averageAccX > 0.01 && player.getX() > 0){
                player.setX(player.getX() - (averageAccX * 50 * Gdx.graphics.getDeltaTime()));
            }

            if(averageAccX < -0.01 && player.getX() < SCREEN_X) {
                player.setX(player.getX() + (-averageAccX * 50 * Gdx.graphics.getDeltaTime()));
            }

        }

        if (game.controlMode == AndroidGame.ControlMode.MANUAL) {
            if (Gdx.input.isTouched()) {
                if (Gdx.input.getX() < SCREEN_X / 2  && player.getX() > 0) {
                    player.setX(player.getX() - (player.speed * Gdx.graphics.getDeltaTime()));

                }
                if (Gdx.input.getX() > SCREEN_X / 2 && player.getX() < SCREEN_X) {
                    player.setX(player.getX() + (player.speed * Gdx.graphics.getDeltaTime()));
                }
            }
        }

        /* Auto jumping or manual - depends on selected jump mode */
        switch (game.jumpMode) {
            case MANUAL: {
                if (Gdx.input.justTouched()) {
                    player.jump();
                }
                break;
            }
            case AUTO: {
                player.jump();
                break;
            }
        }
    }
}
