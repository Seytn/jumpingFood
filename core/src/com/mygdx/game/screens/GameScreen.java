package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.AndroidGame;
import com.mygdx.game.UI.ClickCallback;
import com.mygdx.game.UI.ControlModeSelectButton;
import com.mygdx.game.UI.SimpleLabel;
import com.mygdx.game.entities.JumpPlayer;
import com.mygdx.game.entities.Platform;

import static com.mygdx.game.AndroidGame.GRAVITY;
import static com.mygdx.game.AndroidGame.SCREEN_X;

/**
 * Created by ksikorski on 09.12.2016.
 */

public class GameScreen extends AbstractScreen implements InputProcessor {

    protected Array<Platform> platformArray;
    private Music music;
    private Texture toiletClosedTexture, toiletOpenedTexture;

    private SimpleLabel accXValueLabel;
    private ControlModeSelectButton controlModeSelectButton;

    public GameScreen(AndroidGame game, JumpPlayer player) {
        super(game, player);
        loadData();
        playMusic();

        generatePlatforms();
        initControlTypeSelectButton();
        initAccXValueLabel();

        stage.addActor(player);
    }

    private void playMusic() {
        if (music != null) {
            music.setVolume(0.3f);
            music.play();
            music.setLooping(true);
        }
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

    private void loadData() {
        toiletClosedTexture = game.assets.manager.get("toilet_closed.png",Texture.class);
        toiletOpenedTexture = game.assets.manager.get("toilet_opened.png",Texture.class);
        music = game.assets.manager.get("theme.mp3", Music.class);
    }

    private void initAccXValueLabel() {
        accXValueLabel = new SimpleLabel("");
        stage.addActor(accXValueLabel);
    }

    private void generatePlatforms() {
        platformArray = new Array<Platform>();

        for(int i = 1; i<10; i++){
            Platform p = new Platform(toiletClosedTexture);
            p.setHeight(110);
            p.setX(MathUtils.random(380));
            p.setY(250 * i);
            platformArray.add(p);
            stage.addActor(p);
        }

        Platform p = new Platform(toiletOpenedTexture);
        p.setHeight(100);
        p.setX(MathUtils.random(400));
        p.setY(250 * 10);
        platformArray.add(p);
        stage.addActor(p);
    }

    private void update() {
        handleInput();

        labelPositionUpdate();
        playerPositionUpdate();
        buttonsPositionUpdate();

        stage.act();
    }

    private void labelPositionUpdate() {
        accXValueLabel.setPosition(camera.position.x + 100, camera.position.y + 350);
    }

    private void handleInput() {
        if (game.controlMode == AndroidGame.ControlMode.ACCELEROMETER) {
            float accelerometerX = Gdx.input.getAccelerometerX();
            accXValueLabel.setText(Float.toString(accelerometerX));
            if(accelerometerX > 0.3 && player.getX() > 0){
                if (accelerometerX < 0.6){
                    player.setX(player.getX() - (player.speed / 3 * Gdx.graphics.getDeltaTime()));
                } else {
                    player.setX(player.getX() - (player.speed * Gdx.graphics.getDeltaTime()));
                }
            }
            if(accelerometerX < -0.3 && player.getX() < SCREEN_X){
                if (accelerometerX > -0.6){
                    player.setX(player.getX() + (player.speed / 3 * Gdx.graphics.getDeltaTime()));
                } else {
                    player.setX(player.getX() + (player.speed * Gdx.graphics.getDeltaTime()));
                }
            }
        }

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

    private void buttonsPositionUpdate() {
		controlModeSelectButton.setPosition(camera.position.x + 100, camera.position.y + 250);
    }

    private void playerPositionUpdate() {
        player.setY(player.getY() + (player.jumpSpeed * Gdx.graphics.getDeltaTime()));

        if(player.getY() > 0){
            player.jumpSpeed += GRAVITY;
        } else {
            player.setY(0);
            player.canJump = true;
            player.jumpSpeed = 0;
        }

        for (Platform p : platformArray){
            if(isPlayerOnPlatform(p)){
                player.setY(p.getY() + p.getHeight()-10);
                player.canJump = true;
                player.jumpSpeed = 0;
                if (player.getY() >= 250 * 10){
//                    endGame();

                }
            }
        }
    }

    private boolean isPlayerOnPlatform(Platform p) {
        Rectangle rectPlayer = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        Rectangle rectPlatform = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        return player.jumpSpeed <= 0 && rectPlayer.overlaps(rectPlatform) && player.getY() > p.getY();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        batch.begin();

        for(Platform p : platformArray){
            p.draw(batch);
        }


        stage.draw();
//

//        accXValueLabel.draw(batch);
//        controlModeSelectButton.draw(batch, 1.0f);

        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int tapX, int tapY, int pointer) {
        if (game.controlMode == AndroidGame.ControlMode.MANUAL) {
            if (tapX < SCREEN_X / 2) {
                player.setX(player.getX() - (player.speed * Gdx.graphics.getDeltaTime()));

                return true;
            }
            if (tapX > SCREEN_X / 2) {
                player.setX(player.getX() + (player.speed * Gdx.graphics.getDeltaTime()));
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
