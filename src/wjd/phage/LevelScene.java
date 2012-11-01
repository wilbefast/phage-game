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
import wjd.amb.control.IInteractive;
import wjd.amb.model.Scene;
import wjd.amb.view.ICanvas;
import wjd.math.V2;

/** 
* @author wdyce
* @since 05-Oct-2012
*/
class LevelScene extends Scene 
{
  /* CONSTANTS */
  public static final V2 GRIDSIZE = new V2(100, 60);
  
  /* NESTING */
  private static interface Controller
  {

    public EUpdateResult processStaticInput(IInput input);
    
  }
  
  /* ATTRIBUTES */
  private Tile[][] tilegrid;
  private StrategyCamera camera;
  private Controller controller;

  /* METHODS */

  // constructors
  public LevelScene()
  {    
    // model
    tilegrid = new Tile[(int)GRIDSIZE.y()][(int)GRIDSIZE.x()]; 
    for(int row = 0; row < tilegrid.length; row++)
      for(int col = 0; col < tilegrid[row].length; col++)
        tilegrid[row][col] = new Tile(row, col, (Math.random() > 0.5) 
                                  ? Tile.Type.FLOOR : Tile.Type.WALL);
    
    tilegrid[0][0].setUnit(new Unit(tilegrid[0][0]));
    
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

  private static V2 min = new V2(), max = new V2();
  @Override
  public void render(ICanvas canvas)
  {
    if(!canvas.isCameraActive())
      canvas.setCamera(camera);
    
    // clear the screen
    canvas.clear();
    
    // find out what cells the camera can see
    camera.getVisibleGridCells(min, max, GRIDSIZE, Tile.ISIZE.x());

    // draw each cell
    for(int row = (int)Math.max(0, min.y()); 
      row < Math.min(tilegrid.length, max.y()); row++)
    for(int col = (int)Math.max(0, min.x()); 
      col < Math.min(tilegrid[row].length, max.x()); col++)
        tilegrid[row][col].render(canvas);
  }
}
