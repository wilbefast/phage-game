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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  private Tile[][] tilegrid;
  private StrategyCamera camera;

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
    tilegrid = new Tile[(int) GRIDSIZE.y][(int) GRIDSIZE.x];
    clear();

    // view
    camera = new StrategyCamera(null); // FIXME add boundary
  }

  // mutators
  
  // accessors
  public ICamera getCamera() { return camera; }
  
  public boolean validGridPos(V2 grid_pos)
  {
    return (grid_pos.x >= 0 && grid_pos.y >= 0 
           && grid_pos.y < tilegrid.length && grid_pos.x < tilegrid[0].length);
  }
  
  public Tile perspectiveToTile(V2 perspective_pos)
  {
    V2 grid_pos = camera.getGlobal(perspective_pos).shrink(Tile.SIZE).floor();
    return (validGridPos(grid_pos)
              ? tilegrid[(int) grid_pos.y][(int) grid_pos.x]
              : null);
  }
  
  // clear, load and save
  
  public final void clear()
  {
    // set all tiles as free
    for (int row = 0; row < tilegrid.length; row++)
      for (int col = 0; col < tilegrid[row].length; col++)
        tilegrid[row][col] = new Tile(row, col, Tile.EType.FLOOR, tilegrid);
  }
  
  public void load(File file)
  {
    try
    {
        Object o 
          = (new ObjectInputStream(new FileInputStream(file))).readObject();
        
        if(o instanceof Tile[][])
          this.tilegrid = (Tile[][])o;
        else
          throw new ClassNotFoundException();
    }
    catch (FileNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void save(File file)
  {
    try
    {
      // open specified file and write the object
      (new ObjectOutputStream(new FileOutputStream(file))).writeObject(tilegrid);
    }
    catch (FileNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  /* IMPLEMENTS -- SCENE */
  
  @Override
  public EUpdateResult update(int t_delta)
  {
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE;
  }
  private static V2 min = new V2(), max = new V2();

  @Override
  public void render(ICanvas canvas)
  {    

    // clear the screen
    canvas.clear();

    // find out what cells the camera can see
    canvas.setCamera(camera);
    camera.getVisibleGridCells(min, max, GRIDSIZE, Tile.ISIZE.x);

    // draw each cell relative to the camera
    for (int row = (int) Math.max(0, min.y);
         row < Math.min(tilegrid.length, max.y); row++)
      for (int col = (int) Math.max(0, min.x);
           col < Math.min(tilegrid[row].length, max.x); col++)
        tilegrid[row][col].render(canvas);
    
    // draw the GUI overlay, not relative to the camera
    canvas.setCamera(null);
    ((LevelController)controller).render(canvas);
  }
}
