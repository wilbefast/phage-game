/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage.level;

import java.io.File;
import wjd.amb.AScene;
import wjd.amb.control.EUpdateResult;
import wjd.amb.view.ICamera;
import wjd.amb.view.ICanvas;
import wjd.math.V2;
import wjd.phage.StrategyCamera;
import wjd.phage.editor.EditorController;
import wjd.phage.play.PlayController;

/**
 * @author wdyce
 * @since 05-Oct-2012
 */
public class LevelScene extends AScene
{
  /* CONSTANTS */
  public static final V2 GRIDSIZE = new V2(64, 64);
    
  /* NESTING */
  public static enum EMode
  {
    EDITOR,
    PLAY
  }
  
  /* ATTRIBUTES */
  private StrategyCamera camera;
  public TileGrid tilegrid;

  /* METHODS */
  
  // constructors
  public LevelScene(EMode mode)
  {
    // control
    switch(mode)
    {
      case EDITOR:
        setController(new EditorController(this));
        break;
      case PLAY:
        setController(new PlayController(this));
        break;
    }

    // model
    tilegrid = new TileGrid(GRIDSIZE).clear();

    // view
    camera = new StrategyCamera(null); // FIXME add boundary
  }

  // mutators
  
  // accessors
  
  public ICamera getCamera() { return camera; }

    
  public Tile perspectiveToTile(V2 perspective_pos)
  {
    V2 grid_pos = camera.getGlobal(perspective_pos).shrink(Tile.SIZE).floor();
    return (tilegrid.validGridPos(grid_pos)
              ? tilegrid.tiles[(int) grid_pos.y][(int) grid_pos.x]
              : null);
  }
  
  /* IMPLEMENTS -- SCENE */
  
  @Override
  public EUpdateResult update(int t_delta)
  {
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public void render(ICanvas canvas)
  {    
    // clear the screen
    canvas.clear();

    // find out what cells the camera can see
    canvas.setCamera(camera);
    Tile.Field visible = tilegrid.rectToCells(camera.getView());

    // draw each cell relative to the camera
    if(visible != null) for(Tile t : visible)
      t.render(canvas);

    // turn off camera to draw the GUI overlay
    canvas.setCamera(null);
    ((LevelController)controller).render(canvas);
  }
}
