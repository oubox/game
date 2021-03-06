package com.gcc2ge.game.srceen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gcc2ge.game.MapProcess;
import com.gcc2ge.game.MouseActionList;
import com.gcc2ge.game.MouseActionList.Condition;
import com.gcc2ge.game.MyGdxGame;
import com.gcc2ge.game.TileMapRender;
import com.gcc2ge.game.ai.GridXY;
import com.gcc2ge.game.area.Area;
import com.gcc2ge.game.area.AreaListener;
import com.gcc2ge.game.area.Location;
import com.gcc2ge.game.defaultability.DefaultAbility;
import com.gcc2ge.game.entity.Player;

public class GameScreen implements Screen ,InputProcessor{
	static final String TAG=GameScreen.class.getName();
	static final float PIX_PER_MERTER=2/1.0f;
	
	Game game;
	TileMapRender mapRenderer=null;
	OrthographicCamera mapCamera=null;
	SpriteBatch batch;
	Vector3 touchPosition=new Vector3();
	//debug
	ShapeRenderer shapeRender;
	
	int selectX,selectY;
	
	boolean firstTime=false;
	
	//map process
	MapProcess mapProcess=null;
	//area
	Area area;
	//mousecondition
	MouseActionList.Condition currentCondition;
	//player
	Player player=new Player();
	public GameScreen(Game game){
		//
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		
		this.game=game;
		batch = new SpriteBatch();
		mapRenderer=new TileMapRender(MyGdxGame.loader.map,batch);
		mapCamera=new OrthographicCamera(w/PIX_PER_MERTER,h/PIX_PER_MERTER);
		//注册事件
		InputMultiplexer m=new InputMultiplexer(this,new AreaListener()); 
		Gdx.input.setInputProcessor(m);
		shapeRender=new ShapeRenderer();
		firstTime=true;
		
		//area
		area=new Area();
		//mapprocess
		mapProcess=new MapProcess(MyGdxGame.loader.map);
		mapProcess.setArea(area);
		mapProcess.processTileSets();
		mapProcess.processLayers();
	}
	@Override
	public void render(float delta) {
		player.update();
		mapCamera.position.set(player.positionX, player.positionY, 0);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput();
		mapCamera.update();
		mapRenderer.setView(mapCamera);
		mapRenderer.render();
		
		//debug
		shapeRender.setProjectionMatrix(mapCamera.combined);
		shapeRender.begin(ShapeType.Filled);
		// set to green for full mob health
		shapeRender.setColor(0, 255, 0, 1);
		shapeRender.rect(selectX*16, selectY*16, 16, 16);
		//debug do0r
	/*	for(Portal p:area.portalList){
			Location location=p.targetLocation;
			shapeRender.rect(location.getX()*16, location.getY()*16, 16, 16);
		}*/
		shapeRender.end();
		
		batch.setProjectionMatrix(mapCamera.combined);
		batch.begin();
		player.render(batch);
		batch.end();
		//high
		mapRenderer.renderTileHigh();
	}

	@Override
	public void resize(int width, int height) {
		mapCamera.viewportWidth=width/PIX_PER_MERTER;
		mapCamera.viewportHeight=height/PIX_PER_MERTER;
		mapCamera.zoom=0.68f;
		mapCamera.update();
		firstTime=false;

	}

	@Override
	public void show() {
		

	}

	@Override
	public void hide() {
		

	}

	@Override
	public void pause() {
		

	}

	@Override
	public void resume() {
		

	}

	@Override
	public void dispose() {
		MyGdxGame.loader.dispose();

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
		touchPosition.set(screenX, screenY, 0);
		mapCamera.unproject(touchPosition);
		Gdx.app.log(TAG, touchPosition.x+" "+touchPosition.y);
		selectX=MathUtils.floor(touchPosition.x/16);
		selectY=MathUtils.floor(touchPosition.y/16);
		Gdx.app.log(TAG, selectX+" "+selectY);
//		player.go(selectX, selectY);
		Location location=new Location(area,new GridXY(selectX,selectY));
		DefaultAbility ability = currentCondition.getAbility();
		if(ability.canActivate(player, location)){   
			ability.activate(player, location);
		}
		
		return false;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		int x,y;
		touchPosition.set(screenX, screenY, 0);
		mapCamera.unproject(touchPosition);
		x=MathUtils.floor(touchPosition.x/16);
		y=MathUtils.floor(touchPosition.y/16);
		computeMouseState(x,y);
		
		
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		
		return false;
	}
	//计算鼠标状态
	public void computeMouseState(float x,float y){
		Location location=new Location(area,new GridXY(x,y));
		Condition conditon=MyGdxGame.mouseActionList.getDefalutMouseCondition(player, location);
		currentCondition=conditon;
		String cursor=MyGdxGame.mouseActionList.getMouseCursor(conditon);
		Pixmap p=new Pixmap(Gdx.files.internal(cursor));
		Gdx.input.setCursorImage(p, 0, 0);
	}
	private void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.O)) {
			mapCamera.zoom += 0.02;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.I)) {
			mapCamera.zoom -= 0.02;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			mapCamera.position.x-=10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			mapCamera.position.y-=10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			mapCamera.position.x+=10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			mapCamera.position.y+=10;
		}
	}
}
