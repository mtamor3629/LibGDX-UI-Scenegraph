package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.EffectFactory;
import edu.cornell.gdiac.audio.EffectFilter;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.*;
//import com.badlogic.gdx.graphics.g2d.Free

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	AssetDirectory assets;
	Stage stage;
	Group root;

	AudioEngine engine;

	String demoScript = "";
	@Override
	public void create () {
		engine = (AudioEngine)Gdx.audio;
		this.assets = new AssetDirectory("assets.json");
		assets.loadAssets();
		assets.finishLoading();
		JsonReader reader = new JsonReader();
		/* Parse the scene2d layout */
		JsonValue json = reader.parse(Gdx.files.internal("assets.json"));
		stage = new Stage();
		Scene2Loader scene2 = new Scene2Loader(assets,json,stage);
		try {
			root = scene2.genSceneGraph();
			stage.addActor(root);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		Gdx.input.setInputProcessor(stage);
		stage.setDebugAll(true);

		EffectFactory f = engine.getEffectFactory();
		EffectFilter s = f.createAutoWAH();
		SoundEffect sound1 = assets.getEntry("failurewav", SoundEffect.class);
		sound1.addEffect(sound1.loop(),s);
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
		//****** this line is added to prevent auto zoom from interfering with layout ******//
		stage.getViewport().update(1280, 720, true);
		stage.getRoot().setSize(width, height);
		root.setSize(width, height);
		for (Actor actor : root.getChildren()) {
			System.out.println(actor.getName());
			actor.setSize(width, height);
			if(actor instanceof WidgetGroup)
				((WidgetGroup) actor).layout();
		}
	}
}
