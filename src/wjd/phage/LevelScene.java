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
package wjd.phage;

import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.control.IInput.EKeyCode;
import wjd.amb.control.IInput.KeyPress;
import wjd.amb.control.IInput.MouseClick;
import wjd.amb.model.Scene;
import wjd.amb.view.ICamera;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.math.V2;

/** 
* @author wdyce
* @since 05-Oct-2012
*/
class LevelScene extends Scene 
{
  /* NESTING */
  
  private static class Tile
  {
    public int type;

  }
  
  /* CONSTANTS */
  private static final V2 GRID_SIZE = new V2(100, 60);
  private static final V2 CELL_SIZE = new V2(32, 32);

  /* ATTRIBUTES */
  private Tile[][] tilegrid;
  private StrategyCamera camera;

  /* METHODS */

  // constructors
  public LevelScene()
  {    
    // model
    tilegrid = new Tile[(int)GRID_SIZE.y()][(int)GRID_SIZE.x()]; 
    for(int row = 0; row < tilegrid.length; row++)
      for(int col = 0; col < tilegrid[row].length; col++)
      {  
        tilegrid[row][col] = new Tile();
        tilegrid[row][col].type = (Math.random() > 0.5) ? 1 : 0;
      }
    
    // view
    camera = new StrategyCamera(null); // FIXME add boundary
  }

  // mutators
  
  // accessors
  
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
    if(!canvas.isCameraActive())
      canvas.setCamera(camera);
    
    // clear the screen
    canvas.clear();
    
    // find out what cells the camera can see
    V2 min = new V2(), max = new V2();
    camera.getVisibleGridCells(min, max, GRID_SIZE, 1/CELL_SIZE.x());

    // draw each cell
    for(int row = (int)Math.max(0, min.y()); 
      row < Math.min(tilegrid.length, max.y()); row++)
    for(int col = (int)Math.max(0, min.x()); 
      col < Math.min(tilegrid[row].length, max.x()); col++)
        renderTile(row, col, canvas);
  }

  @Override
  public EUpdateResult processStaticInput(IInput input)
  {
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE; 
  }

  @Override
  public EUpdateResult processKeyPress(KeyPress event)
  {
    if(event.key == EKeyCode.ENTER && event.state)
    {
      next = new TitleScene();
      return EUpdateResult.STOP;
    }
    else if(event.key == IInput.EKeyCode.ESC && event.state)
      return EUpdateResult.STOP;
    
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE;
  }

  @Override
  public EUpdateResult processMouseClick(MouseClick event)
  {
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
 
  private static Rect stamp = new Rect();
  private static V2 stamp_pos = new V2(), stamp_size = new V2();
  private void renderTile(int row, int col, ICanvas canvas)
  {
    Tile tile = tilegrid[row][col];
    
    // draw tile -- setup context
    canvas.setColour(tile.type == 0 ? Colour.RED : Colour.BLUE);
    
    // draw tile
    stamp_pos.xy(col*CELL_SIZE.x(), row*CELL_SIZE.y());
    stamp_size.reset(CELL_SIZE).scale(camera.getZoom()+1);
    stamp.reset(stamp_pos, stamp_size);
    canvas.box(stamp);
  }
}
