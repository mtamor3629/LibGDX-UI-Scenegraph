package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.ui.UI;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	Stage stage;
	
	@Override
	public void create () {
		JsonReader reader = new JsonReader();
		JsonValue json = reader.parse(Gdx.files.internal("assets.json"));
		stage = UI.genSceneGraph(json);
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
		img.dispose();
	}

	@Override
	public void resize(int width, int height){
		stage.getViewport().update(width, height, true);
	}
}
