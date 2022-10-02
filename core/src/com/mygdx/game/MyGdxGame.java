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
//import com.badlogic.gdx.graphics.g2d.Free

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	AssetDirectory assets;

	Stage stage;
	
	@Override
	public void create () {
		this.assets = new AssetDirectory("assets.json");
		assets.loadAssets();
		assets.finishLoading();
		JsonReader reader = new JsonReader();
		JsonValue json = reader.parse(Gdx.files.internal("assets_float.json"));
		stage = new Stage();
		stage.addActor(Scene2Loader.genSceneGraph(json,assets,stage));
		//stage.addActor(Scene2Loader.genAltAltSceneGraph(assets,stage));
		Gdx.input.setInputProcessor(stage);
		stage.setDebugAll(true);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		//if(stage.)
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
