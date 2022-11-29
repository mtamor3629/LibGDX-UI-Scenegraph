package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.*;
import java.util.Timer;
//import com.badlogic.gdx.graphics.g2d.Free

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	AssetDirectory assets;

	Stage stage;

	//String demoScript = "lab_startmenu.setRotation(stage.getViewport().getScreenWidth());\n";

	String demoScript = "";
	@Override
	public void create () {
		this.assets = new AssetDirectory("assets.json");
		assets.loadAssets();
		assets.finishLoading();
		JsonReader reader = new JsonReader();
		JsonValue json = reader.parse(Gdx.files.internal("assets_copy.json"));
		stage = new Stage();
		try {
			stage.addActor(Scene2Loader.genSceneGraph(json,assets,stage));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		Gdx.input.setInputProcessor(stage);
		stage.setDebugAll(true);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		assets.dispose();
		img.dispose();
		stage.dispose();
	}

	@Override
	public void resize(int width, int height){
		stage.getViewport().update(width, height, true);
	}
}
