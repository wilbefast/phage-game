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
import wjd.math.Rect;
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
  
  /** Which cells of the grid are inside the rectangle?
   * 
   * @param rect the rectangle which we want to draw cells from.
   * @return a Tile.Field structure containing a pair of 
   * coordinates corresponding to the top-left- and bottom-right-most cells in
   * the grid.
   */
  public Tile.Field rectToCells(Rect rect)
  {
    // top-left cell
    V2.cache[0].xy((int)Math.max(0, rect.x*Tile.ISIZE.x-1),
                                 (int)Math.max(0, rect.y*Tile.ISIZE.y-1));
    
    // bottom-right cell
    V2.cache[1].xy((int)Math.min(GRIDSIZE.x-1, rect.endx()*Tile.ISIZE.x+1),
                              (int)Math.min(GRIDSIZE.y-1, rect.endy()*Tile.ISIZE.y+1));
    
    // save the references in the pre-allocated cache, then return them
    Tile.Field.cache[0].reset(tilegrid[(int)V2.cache[0].y][(int)V2.cache[0].x], 
                              tilegrid[(int)V2.cache[1].y][(int)V2.cache[1].x]);
    return Tile.Field.cache[0];
  }
  
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
  
  @Override
  public void render(ICanvas canvas)
  {    
    // clear the screen
    canvas.clear();

    // find out what cells the camera can see
    canvas.setCamera(camera);
    Tile.Field visible = rectToCells(camera.getView());

    /* TODO!!  */
    // draw each cell relative to the camera
    for(Tile t : visible)
      t.render(canvas);
    /*for (int row = (int)visible.first.position.y; row < (int)visible.last.position.y; row++)
    for (int col = (int)visible.first.position.x; col < (int)visible.last.position.x; col++)
      tilegrid[row][col].render(canvas);*/
    
    // turn off camera to draw the GUI overlay
    canvas.setCamera(null);
    ((LevelController)controller).render(canvas);
  }
}
